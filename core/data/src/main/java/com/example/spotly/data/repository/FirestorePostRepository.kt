package com.example.spotly.data.repository

import com.example.spotly.domain.repository.PostRepository
import com.example.spotly.domain.repository.UploadedImage
import com.example.spotly.network.PostDto

import com.example.spotly.domain.model.Post
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.AppError
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class FirestorePostRepository(services: com.example.spotly.network.FirebaseServices) : PostRepository {
    private val db = services.firestore

    override fun newId(): String = db.collection("posts").document().id

    override fun save(id: String, uid: String, image: UploadedImage, description: String,
             location: com.example.spotly.domain.model.LocationPoint?,
             onResult: (AppResult<Unit>) -> Unit) {
        db.collection("users").document(uid).get().addOnSuccessListener { user ->
            val username = user.getString("username")
            if (username == null) {
                onResult(AppResult.Error(AppError.PostSaveFailed))
                return@addOnSuccessListener
            }
            val ref = db.collection("posts").document(id)
            // Un ID estable evita duplicados si se reintenta después de perder la respuesta.
            db.runTransaction { transaction ->
                if (!transaction.get(ref).exists()) {
                    transaction.set(ref, image.toCreatePostDto(uid, username, description, location))
                }
            }.addOnSuccessListener { onResult(AppResult.Success(Unit)) }.addOnFailureListener { onResult(AppResult.Error(AppError.PostSaveFailed)) }
        }.addOnFailureListener { onResult(AppResult.Error(AppError.PostSaveFailed)) }
    }

    override fun observeFeed() = observeQuery(db.collection("posts")
        .orderBy("createdAt", Query.Direction.DESCENDING).limit(50))

    // No se limita al feed: el total del perfil incluye todas las publicaciones propias.
    override fun observeUserPosts(uid: String) = observeQuery(
        db.collection("posts").whereEqualTo("authorId", uid)
    )

    private fun observeQuery(query: Query) = callbackFlow<AppResult<List<Post>>> {
        val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) trySend(AppResult.Error(AppError.FeedLoadFailed))
                else if (snapshot != null) {
                    val result = runCatching {
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(PostDto::class.java)?.toDomain(doc.id)
                        }.sortedByDescending { it.createdAt }
                    }
                    trySend(result.fold(
                        onSuccess = { AppResult.Success(it) },
                        onFailure = { AppResult.Error(AppError.FeedLoadFailed) }
                    ))
                }
            }
        awaitClose { listener.remove() }
    }
}
