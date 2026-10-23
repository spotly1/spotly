package com.example.spotly.database

import androidx.room.Entity

/** Cada cuenta tiene su propio feed; nunca se mezclan las filas al cambiar de usuario. */
@Entity(tableName = "cached_posts", primaryKeys = ["accountId", "id"])
data class PostEntity(
    val accountId: String, val id: String, val authorId: String,
    val username: String, val profileImageUrl: String,
    val imageUrl: String, val imagePublicId: String, val description: String,
    val createdAtMillis: Long?, val latitude: Double?, val longitude: Double?
)
