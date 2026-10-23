package com.example.spotly.database

import androidx.room.*

@Entity(tableName = "cached_feeds")
data class FeedEntity(@PrimaryKey val accountId: String, val hasFollowing: Boolean)

data class CachedFeed(
    @Embedded val feed: FeedEntity,
    @Relation(parentColumn = "accountId", entityColumn = "accountId") val posts: List<PostEntity>
)
