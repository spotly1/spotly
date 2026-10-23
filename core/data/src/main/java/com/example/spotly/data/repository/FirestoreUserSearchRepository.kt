package com.example.spotly.data.repository

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.User
import com.example.spotly.domain.repository.UserSearchRepository
import com.example.spotly.network.FirebaseServices
import com.example.spotly.network.UserDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirestoreUserSearchRepository(private val services: FirebaseServices) : UserSearchRepository {
    override suspend fun search(prefix: String): AppResult<List<User>> {
        if (services.auth.currentUser == null) return AppResult.Error(AppError.Unauthenticated)
        return try {
            val snapshot = services.firestore.collection("users")
                .orderBy("username").startAt(prefix).endAt(prefix + "\uf8ff")
                .limit(20).get().await()
            AppResult.Success(snapshot.documents.mapNotNull { document ->
                document.toObject(UserDto::class.java)?.toPublicDomain(document.id)
            })
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            AppResult.Error(AppError.UserSearchFailed)
        }
    }
}
