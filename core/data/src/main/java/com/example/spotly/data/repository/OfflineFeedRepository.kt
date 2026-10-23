package com.example.spotly.data.repository

import com.example.spotly.database.FeedDao
import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Room es la fuente de lectura; solo respuestas confirmadas del servidor reemplazan el feed. */
@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFeedRepository(
    private val posts: PostRepository,
    private val follows: FollowRepository,
    private val auth: AuthRepository,
    private val connection: ConnectionRepository,
    private val dao: FeedDao
) : FeedRepository {
    private data class Sync(val online: Boolean, val fresh: Boolean = false, val error: AppError? = null)

    override fun observe(): Flow<AppResult<FeedPage>> = channelFlow {
        val account = auth.currentUserId()
        if (account == null) {
            send(AppResult.Error(AppError.Unauthenticated))
            return@channelFlow
        }
        val sync = MutableStateFlow(Sync(connection.isOnline()))
        launch {
            combine(dao.observe(account), sync) { cached, status ->
                when {
                    auth.currentUserId() != account -> AppResult.Error(AppError.Unauthenticated)
                    cached != null -> AppResult.Success(FeedPage(
                        cached.posts.map { it.toDomain() }
                            .sortedWith(compareByDescending<Post> { it.createdAt }.thenBy { it.id }),
                        cached.feed.hasFollowing, !status.fresh || !status.online, status.error))
                    status.error != null -> AppResult.Error(status.error)
                    !status.online -> AppResult.Error(AppError.OfflineCacheEmpty)
                    else -> null
                }
            }.filterNotNull().catch { emit(AppResult.Error(AppError.CacheFailed)) }.collect { send(it) }
        }
        connection.observe().collectLatest { online ->
            sync.value = Sync(online)
            if (!online || auth.currentUserId() != account) return@collectLatest
            try {
                follows.observeFollowing(account).distinctUntilChanged().collectLatest { relations ->
                    when (relations) {
                        is AppResult.Error -> sync.value = Sync(true, error = relations.error)
                        is AppResult.Success -> {
                            val authors = relations.data - account
                            dao.keepAuthors(account, authors.toList())
                            posts.observeFeed(authors).collect { result ->
                                if (auth.currentUserId() != account) return@collect
                                when (result) {
                                    is AppResult.Error -> sync.value = Sync(true, error = result.error)
                                    is AppResult.Success -> {
                                        dao.replace(account, authors.isNotEmpty(), result.data.map { it.toEntity(account) })
                                        sync.value = Sync(true, fresh = true)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { sync.value = Sync(true, error = AppError.CacheFailed) }
        }
    }
}
