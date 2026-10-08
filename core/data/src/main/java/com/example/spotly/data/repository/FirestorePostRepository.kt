package com.example.spotly.data.repository

import com.example.spotly.domain.repository.PostRepository
import com.example.spotly.domain.repository.UploadedImage
import com.example.spotly.network.PostDto
import com.example.spotly.domain.model.Post
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.LocationPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

class FirestorePostRepository(services: com.example.spotly.network.FirebaseServices) : PostRepository {
    private val db = services.firestore

    override fun newId(): String = db.collection("posts").document().id

    override fun save(
        id: String,
        uid: String,
        image: UploadedImage,
        description: String,
        location: LocationPoint?,
        onResult: (AppResult<Unit>) -> Unit
    ) {
        val ref = db.collection("posts").document(id)

        db.runTransaction { transaction ->
            if (!transaction.get(ref).exists()) {
                transaction.set(
                    ref,
                    image.toCreatePostDto(uid, description, location)
                )
            }
        }.addOnSuccessListener {
            onResult(AppResult.Success(Unit))
        }.addOnFailureListener {
            onResult(AppResult.Error(AppError.PostSaveFailed))
        }
    }

    override fun observeFeed() = observeQuery(db.collection("posts")
        .orderBy("createdAt", Query.Direction.DESCENDING).limit(50))

    override fun observeUserPosts(uid: String) = observeQuery(
        db.collection("posts")
            .whereEqualTo("authorId", uid)
            .limit(50)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeQuery(query: Query): Flow<AppResult<List<Post>>> {
        val authorCache = mutableMapOf<String, Pair<String, String>>()

        return callbackFlow {
            val listener = query.addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        trySend(AppResult.Error(AppError.FeedLoadFailed))
                    }

                    snapshot != null -> {
                        trySend(
                            runCatching {
                                snapshot.documents.mapNotNull { doc ->
                                    doc.toObject(PostDto::class.java)
                                        ?.toDomain(doc.id)
                                }
                            }.fold(
                                onSuccess = { AppResult.Success(it) },
                                onFailure = { AppResult.Error(AppError.FeedLoadFailed) }
                            )
                        )
                    }
                }
            }

            awaitClose { listener.remove() }
        }.mapLatest { result ->
            when (result) {
                is AppResult.Error -> result

                is AppResult.Success -> {
                    try {
                        val posts = result.data

                        val missingAuthors = posts
                            .map { it.authorId }
                            .filter { it.isNotBlank() && it !in authorCache }
                            .distinct()

                        val fetchedAuthors = coroutineScope {
                            missingAuthors.map { uid ->
                                async {
                                    val document = db.collection("users")
                                        .document(uid)
                                        .get()
                                        .await()

                                    uid to Pair(
                                        document.getString("username").orEmpty(),
                                        document.getString("profileImageUrl").orEmpty()
                                    )
                                }
                            }.awaitAll()
                        }

                        authorCache.putAll(fetchedAuthors)

                        val enrichedPosts = posts.map { post ->
                            val author = authorCache[post.authorId]

                            post.copy(
                                username = author?.first.orEmpty(),
                                profileImageUrl = author?.second.orEmpty()
                            )
                        }.sortedByDescending { it.createdAt }

                        AppResult.Success(enrichedPosts)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        AppResult.Error(AppError.FeedLoadFailed)
                    }
                }
            }
        }
    }

}
