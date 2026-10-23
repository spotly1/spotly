package com.example.spotly.data.repository

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.FollowSummary
import com.example.spotly.domain.model.FollowListType
import com.example.spotly.domain.model.User
import com.example.spotly.network.UserDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import com.example.spotly.domain.repository.FollowRepository
import com.example.spotly.network.FirebaseServices
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

class FirestoreFollowRepository(private val services: FirebaseServices,
    private val connection: com.example.spotly.domain.repository.ConnectionRepository) : FollowRepository {
    private val follows = services.firestore.collection("follows")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUsers(uid: String, type: FollowListType): Flow<AppResult<List<User>>> {
        val ids = if (type == FollowListType.Followers)
            observeIds(follows.whereEqualTo("followedId", uid), "followerId")
        else observeFollowing(uid)
        return ids.mapLatest { result ->
            when (result) {
                is AppResult.Error -> result
                is AppResult.Success -> try {
                    val users = coroutineScope {
                        val permits = Semaphore(8)
                        result.data.map { userId ->
                            async {
                                permits.withPermit {
                                    val doc = services.firestore.collection("users").document(userId).get().await()
                                    doc.toObject(UserDto::class.java)?.toPublicDomain(doc.id)
                                }
                            }
                        }.awaitAll().filterNotNull()
                    }
                    AppResult.Success(users.sortedBy { it.username.lowercase() })
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AppResult.Error(AppError.FollowLoadFailed)
                }
            }
        }
    }

    override fun observeFollowing(uid: String) = observeIds(
        follows.whereEqualTo("followerId", uid), "followedId"
    )

    override fun observeProfile(uid: String): Flow<AppResult<FollowSummary>> = combine(
        observeIds(follows.whereEqualTo("followedId", uid), "followerId"),
        observeFollowing(uid)
    ) { followers, following ->
        when {
            followers is AppResult.Error -> followers
            following is AppResult.Error -> following
            followers is AppResult.Success && following is AppResult.Success -> AppResult.Success(
                FollowSummary(followers.data.size, following.data.size,
                    services.auth.currentUser?.uid in followers.data)
            )
            else -> AppResult.Error(AppError.FollowLoadFailed)
        }
    }

    private fun observeIds(query: Query, field: String): Flow<AppResult<Set<String>>> = callbackFlow {
        val listener = query.addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null || snapshot == null) trySend(AppResult.Error(AppError.FollowLoadFailed))
            else if (!snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites())
                trySend(AppResult.Success(snapshot.documents.mapNotNull { it.getString(field) }.toSet()))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun setFollowing(uid: String, following: Boolean): AppResult<Unit> {
        if (!connection.isOnline()) return AppResult.Error(AppError.Offline)
        val current = services.auth.currentUser?.uid ?: return AppResult.Error(AppError.Unauthenticated)
        if (uid.isBlank() || uid == current) return AppResult.Error(AppError.FollowUpdateFailed)
        return try {
            val ref = follows.document("${current}_${uid}")
            val notice = services.firestore.collection("notifications").document("follow_${current}_${uid}")
            // El mismo ID y una transacción hacen que reintentar no duplique relaciones.
            services.firestore.runTransaction { transaction ->
                val exists = transaction.get(ref).exists()
                if (following && !exists) {
                    transaction.set(ref, mapOf("followerId" to current, "followedId" to uid,
                        "createdAt" to FieldValue.serverTimestamp()))
                    transaction.set(notice, notificationData(current, uid, "follow"))
                } else if (!following && exists) {
                    transaction.delete(ref)
                    transaction.delete(notice)
                }
                Unit
            }.await()
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Error(AppError.FollowUpdateFailed)
        }
    }
}
