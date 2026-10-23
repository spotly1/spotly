package com.example.spotly.data.repository

import com.example.spotly.database.PostEntity
import com.example.spotly.domain.model.LocationPoint
import com.example.spotly.domain.model.Post
import java.time.Instant

internal fun Post.toEntity(accountId: String) = PostEntity(accountId, id, authorId, username,
    profileImageUrl, imageUrl, imagePublicId, description, createdAt?.toEpochMilli(),
    location?.latitude, location?.longitude)

internal fun PostEntity.toDomain() = Post(id, authorId, username, profileImageUrl,
    imageUrl, imagePublicId, description, createdAtMillis?.let(Instant::ofEpochMilli),
    latitude?.let { lat -> longitude?.let { lon -> LocationPoint(lat, lon) } })
