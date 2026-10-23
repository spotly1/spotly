package com.example.spotly.data.repository

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.NotificationRepository
import com.example.spotly.network.FirebaseServices
import com.example.spotly.network.NotificationDto
import com.example.spotly.network.UserDto
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class FirestoreNotificationRepository(private val services: FirebaseServices,
    private val connection: com.example.spotly.domain.repository.ConnectionRepository) : NotificationRepository {
    private val notices = services.firestore.collection("notifications")

    override fun observe(onlyUnread: Boolean) = observeItems(onlyUnread, false)

    override fun observeForAlerts() = observeItems(false, true)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeItems(onlyUnread: Boolean, confirmedOnly: Boolean): Flow<AppResult<List<AppNotification>>> {
        val uid = services.auth.currentUser?.uid ?: return flowOf(AppResult.Error(AppError.Unauthenticated))
        val actors = mutableMapOf<String, UserDto>()
        val query = notices.whereEqualTo("recipientId", uid).let { if (onlyUnread) it.whereEqualTo("read", false) else it }
        return listen(query
            .orderBy("createdAt", Query.Direction.DESCENDING).limit(50), confirmedOnly).mapLatest { result ->
            when (result) {
                is AppResult.Error -> result
                is AppResult.Success -> try {
                    val items = result.data.documents.mapNotNull { doc ->
                        val dto = doc.toObject(NotificationDto::class.java) ?: return@mapNotNull null
                        val actor = actors[dto.actorId] ?: services.firestore.collection("users").document(dto.actorId)
                            .get().await().toObject(UserDto::class.java).orEmptyUser().also { actors[dto.actorId] = it }
                        dto.toDomain(doc.id, actor)
                    }
                    AppResult.Success(items)
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { AppResult.Error(AppError.NotificationsLoadFailed) }
            }
        }
    }

    override fun observeUnread(): Flow<AppResult<Boolean>> {
        val uid = services.auth.currentUser?.uid ?: return flowOf(AppResult.Error(AppError.Unauthenticated))
        // Independiente de las últimas 50: un aviso más antiguo sin leer también activa la campana.
        return listen(notices.whereEqualTo("recipientId", uid).whereEqualTo("read", false)
            .orderBy("createdAt", Query.Direction.DESCENDING).limit(1)).map {
            when (it) {
                is AppResult.Error -> it
                is AppResult.Success -> AppResult.Success(!it.data.isEmpty)
            }
        }
    }

    private fun listen(query: Query, confirmedOnly: Boolean = false): Flow<AppResult<QuerySnapshot>> = callbackFlow {
        val listener = query.addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null || snapshot == null) trySend(AppResult.Error(AppError.NotificationsLoadFailed))
            else if (!confirmedOnly || (!snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()))
                trySend(AppResult.Success(snapshot))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun markRead(id: String): AppResult<Unit> {
        if (!connection.isOnline()) return AppResult.Error(AppError.Offline)
        val uid = services.auth.currentUser?.uid ?: return AppResult.Error(AppError.Unauthenticated)
        return try {
            val ref = notices.document(id)
            val found = services.firestore.runTransaction { tx ->
                val doc = tx.get(ref)
                if (!doc.exists() || doc.getString("recipientId") != uid) return@runTransaction false
                if (doc.getBoolean("read") != true) tx.update(ref, "read", true)
                true
            }.await()
            if (found) AppResult.Success(Unit) else AppResult.Error(AppError.NotificationReadFailed)
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { AppResult.Error(AppError.NotificationReadFailed) }
    }

    private fun UserDto?.orEmptyUser() = this ?: UserDto()
}
