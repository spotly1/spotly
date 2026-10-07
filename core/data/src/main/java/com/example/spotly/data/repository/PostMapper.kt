package com.example.spotly.data.repository

import com.example.spotly.domain.model.Post
import com.example.spotly.domain.model.LocationPoint
import com.example.spotly.network.PostDto
import java.time.Instant
import com.example.spotly.domain.repository.UploadedImage
import com.example.spotly.network.CreatePostDto
import com.google.firebase.firestore.GeoPoint

internal fun PostDto.toDomain(id: String) = Post(
    id = id,
    authorId = authorId,
    username = username,
    imageUrl = imageUrl,
    imagePublicId = imagePublicId,
    description = description,
    createdAt = createdAt?.let { Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong()) },
    location = location?.let { LocationPoint(it.latitude, it.longitude) }
)

internal fun UploadedImage.toCreatePostDto(
    authorId: String,
    username: String,
    description: String,
    location: LocationPoint?
) = CreatePostDto(
    authorId = authorId,
    username = username,
    imageUrl = url,
    imagePublicId = publicId,
    description = description,
    location = location?.let { GeoPoint(it.latitude, it.longitude) }
)
