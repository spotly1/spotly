package com.example.spotly.data.repository

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.*
import com.example.spotly.network.FirebaseServices
import com.example.spotly.network.PostDto
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirestoreExploreRepository(
    private val services: FirebaseServices,
    private val connection: ConnectionRepository
) : ExploreRepository {
    override suspend fun load(excludedAuthors: Set<String>, cursor: ExploreCursor?): AppResult<ExplorePage> {
        if (!connection.isOnline()) return AppResult.Error(AppError.Offline)
        val uid = services.auth.currentUser?.uid ?: return AppResult.Error(AppError.Unauthenticated)
        return try {
            var query = services.firestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
                .limit(50)
            if (cursor != null) query = query.startAfter(
                Timestamp(cursor.createdAt.epochSecond, cursor.createdAt.nano), cursor.id)
            val snapshot = withTimeout(20_000) { query.get(Source.SERVER).await() }
            val next = if (snapshot.size() < 50) null else snapshot.documents.last().let { last ->
                val date = requireNotNull(last.getTimestamp("createdAt"))
                ExploreCursor(Instant.ofEpochSecond(date.seconds, date.nanoseconds.toLong()), last.id)
            }
            val posts = snapshot.documents.mapNotNull { it.toObject(PostDto::class.java)?.toDomain(it.id) }
                .filter { it.authorId.isNotBlank() && it.authorId !in excludedAuthors && it.authorId != uid }
            val authors = coroutineScope {
                val permits = Semaphore(8)
                posts.map { it.authorId }.distinct().map { author ->
                    async {
                        permits.withPermit {
                            val user = withTimeout(20_000) {
                                services.firestore.collection("users").document(author).get(Source.SERVER).await()
                            }
                            author to (user.getString("username").orEmpty() to user.getString("profileImageUrl").orEmpty())
                        }
                    }
                }.awaitAll().toMap()
            }
            if (services.auth.currentUser?.uid != uid) AppResult.Error(AppError.Unauthenticated)
            else AppResult.Success(ExplorePage(posts.map { post ->
                val author = authors[post.authorId]
                post.copy(username = author?.first.orEmpty(), profileImageUrl = author?.second.orEmpty())
            }, next))
        } catch (_: TimeoutCancellationException) { AppResult.Error(AppError.ExploreLoadFailed) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { AppResult.Error(if (connection.isOnline()) AppError.ExploreLoadFailed else AppError.Offline) }
    }
}
