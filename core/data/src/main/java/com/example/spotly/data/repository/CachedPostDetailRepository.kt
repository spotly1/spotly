package com.example.spotly.data.repository

import com.example.spotly.database.FeedDao
import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.*
import kotlinx.coroutines.CancellationException

class CachedPostDetailRepository(
    private val remote: PostDetailRepository,
    private val auth: AuthRepository,
    private val connection: ConnectionRepository,
    private val dao: FeedDao
) : PostDetailRepository {
    override suspend fun get(id: String): AppResult<Post> {
        val uid = auth.currentUserId() ?: return AppResult.Error(AppError.Unauthenticated)
        if (connection.isOnline()) {
            val result = remote.get(id)
            if (result is AppResult.Error && result.error in setOf(AppError.PostNotFound, AppError.PostPermissionDenied)) {
                cacheWrite { dao.removePost(uid, id) }
            }
            return if (auth.currentUserId() == uid) result else AppResult.Error(AppError.Unauthenticated)
        }
        return try {
            val post = dao.getPost(uid, id)
            if (auth.currentUserId() != uid) AppResult.Error(AppError.Unauthenticated)
            else post?.let { AppResult.Success(it.toDomain()) } ?: AppResult.Error(AppError.OfflineCacheEmpty)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { AppResult.Error(AppError.CacheFailed) }
    }

    override suspend fun updateDescription(id: String, uid: String, description: String): AppResult<Unit> {
        if (!connection.isOnline()) return AppResult.Error(AppError.Offline)
        val result = remote.updateDescription(id, uid, description)
        if (result is AppResult.Success) cacheWrite { dao.updateDescription(uid, id, description) }
        return result
    }

    override suspend fun delete(id: String, uid: String): AppResult<Unit> {
        if (!connection.isOnline()) return AppResult.Error(AppError.Offline)
        val result = remote.delete(id, uid)
        if (result is AppResult.Success) cacheWrite { dao.removePost(uid, id) }
        return result
    }

    // Una escritura confirmada por Firebase no debe informarse como fallida por un error local.
    private suspend fun cacheWrite(block: suspend () -> Unit) {
        try { block() } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { /* La siguiente sincronización reconstruye la caché. */ }
    }
}
