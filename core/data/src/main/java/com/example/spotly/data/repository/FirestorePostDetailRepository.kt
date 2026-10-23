package com.example.spotly.data.repository

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.Post
import com.example.spotly.domain.repository.PostDetailRepository
import com.example.spotly.network.FirebaseServices
import com.example.spotly.network.PostDto
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirestorePostDetailRepository(services: FirebaseServices) : PostDetailRepository {
    private val db = services.firestore

    override suspend fun get(id: String): AppResult<Post> = safely(AppError.PostLoadFailed) {
        val document = db.collection("posts").document(id).get(com.google.firebase.firestore.Source.SERVER).await()
        val post = document.toObject(PostDto::class.java)?.toDomain(document.id)
            ?: return@safely AppResult.Error(AppError.PostNotFound)
        val author = db.collection("users").document(post.authorId).get(com.google.firebase.firestore.Source.SERVER).await()
        AppResult.Success(post.copy(
            username = author.getString("username").orEmpty(),
            profileImageUrl = author.getString("profileImageUrl").orEmpty()
        ))
    }

    override suspend fun updateDescription(id: String, uid: String, description: String): AppResult<Unit> =
        mutate(id, uid, description)

    override suspend fun delete(id: String, uid: String): AppResult<Unit> = mutate(id, uid, null)

    // La lectura y la escritura son atómicas. El UID y las reglas también se verifican en Firebase.
    private suspend fun mutate(id: String, uid: String, description: String?): AppResult<Unit> =
        safely(if (description == null) AppError.PostDeleteFailed else AppError.PostUpdateFailed) {
            val ref = db.collection("posts").document(id)
            db.runTransaction<AppResult<Unit>> { transaction ->
                val post = transaction.get(ref)
                when {
                    !post.exists() -> AppResult.Error(AppError.PostNotFound)
                    post.getString("authorId") != uid -> AppResult.Error(AppError.PostPermissionDenied)
                    else -> {
                        if (description == null) transaction.delete(ref)
                        else transaction.update(ref, "description", description)
                        AppResult.Success(Unit)
                    }
                }
            }.await()
        }

    private suspend fun <T> safely(fallback: AppError, block: suspend () -> AppResult<T>): AppResult<T> =
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            val reason = if (error is FirebaseFirestoreException &&
                error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            ) AppError.PostPermissionDenied else fallback
            AppResult.Error(reason)
        }
}
