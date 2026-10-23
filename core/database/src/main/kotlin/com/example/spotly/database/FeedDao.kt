package com.example.spotly.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FeedDao {
    @Transaction
    @Query("SELECT * FROM cached_feeds WHERE accountId = :accountId")
    abstract fun observe(accountId: String): Flow<CachedFeed?>

    @Query("SELECT * FROM cached_posts WHERE accountId = :accountId AND id = :id")
    abstract suspend fun getPost(accountId: String, id: String): PostEntity?

    @Upsert
    protected abstract suspend fun upsertFeed(feed: FeedEntity)
    @Upsert
    protected abstract suspend fun upsertPosts(posts: List<PostEntity>)
    @Query("DELETE FROM cached_posts WHERE accountId = :accountId")
    protected abstract suspend fun clearPosts(accountId: String)

    @Transaction
    open suspend fun replace(accountId: String, hasFollowing: Boolean, posts: List<PostEntity>) {
        require(posts.size <= 50 && posts.all { it.accountId == accountId })
        clearPosts(accountId)
        upsertPosts(posts)
        upsertFeed(FeedEntity(accountId, hasFollowing))
    }

    @Query("DELETE FROM cached_posts WHERE accountId = :accountId AND authorId NOT IN (:authors)")
    abstract suspend fun keepAuthors(accountId: String, authors: List<String>)

    @Query("DELETE FROM cached_posts WHERE accountId = :accountId AND id = :id")
    abstract suspend fun removePost(accountId: String, id: String)

    @Query("UPDATE cached_posts SET description = :description WHERE accountId = :accountId AND id = :id")
    abstract suspend fun updateDescription(accountId: String, id: String, description: String)
}
