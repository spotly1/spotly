package com.example.spotly.data.repository

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.LikeRepository
import com.example.spotly.network.FirebaseServices
import com.example.spotly.network.UserDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreLikeRepository(private val services: FirebaseServices,
    private val connection: com.example.spotly.domain.repository.ConnectionRepository) : LikeRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUsers(postId: String): Flow<AppResult<List<User>>> = callbackFlow<AppResult<List<String>>> {
        val listener = services.firestore.collection("posts").document(postId).collection("likes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) trySend(AppResult.Error(AppError.LikesLoadFailed))
                else trySend(AppResult.Success(snapshot.documents.map { it.id }))
            }
        awaitClose { listener.remove() }
    }.mapLatest { result ->
        when (result) {
            is AppResult.Error -> result
            is AppResult.Success -> try {
                val users = coroutineScope {
                    val permits = Semaphore(8)
                    result.data.map { uid ->
                        async {
                            permits.withPermit {
                                val doc = services.firestore.collection("users").document(uid).get().await()
                                doc.toObject(UserDto::class.java)?.toPublicDomain(doc.id)
                            }
                        }
                    }.awaitAll().filterNotNull()
                }
                AppResult.Success(users.sortedBy { it.username.lowercase() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppResult.Error(AppError.LikesLoadFailed)
            }
        }
    }

    override fun observe(postId: String): Flow<AppResult<PostLikes>> = callbackFlow {
        val uid = services.auth.currentUser?.uid
        if (uid == null) {
            trySend(AppResult.Error(AppError.Unauthenticated))
            close()
            return@callbackFlow
        }
        val listener = services.firestore.collection("posts").document(postId).collection("likes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) trySend(AppResult.Error(AppError.LikesLoadFailed))
                else trySend(AppResult.Success(PostLikes(snapshot.size(), snapshot.documents.any { it.id == uid })))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun setLiked(postId: String, liked: Boolean): AppResult<Unit> {
        if (!connection.isOnline()) return AppResult.Error(AppError.Offline)
        val uid = services.auth.currentUser?.uid ?: return AppResult.Error(AppError.Unauthenticated)
        return try {
            val post = services.firestore.collection("posts").document(postId)
            val like = post.collection("likes").document(uid)
            val found = services.firestore.runTransaction { transaction ->
                // Leer el post evita crear un like si se elimina durante la operación.
                val postDoc = transaction.get(post)
                if (!postDoc.exists()) return@runTransaction false
                val recipient = postDoc.getString("authorId") ?: return@runTransaction false
                val postImageUrl = postDoc.getString("imageUrl").orEmpty()
                val notice = services.firestore.collection("notifications").document("like_${postId}_${uid}")
                val exists = transaction.get(like).exists()
                if (liked && !exists) {
                    transaction.set(like, mapOf("createdAt" to FieldValue.serverTimestamp()))
                    if (recipient != uid) {
                        transaction.set(
                            notice,
                            notificationData(uid, recipient, "like", postId, postImageUrl)
                        )
                    }
                } else if (!liked && exists) {
                    transaction.delete(like)
                    if (recipient != uid)
                        transaction.delete(notice)
                }
                true
            }.await()
            if (found) AppResult.Success(Unit) else AppResult.Error(AppError.PostNotFound)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Error(AppError.LikeUpdateFailed)
        }
    }
}
