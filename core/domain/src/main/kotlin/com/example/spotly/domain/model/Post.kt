package com.example.spotly.domain.model

import java.time.Instant

data class Post(
    val id: String = "",
    val authorId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val imagePublicId: String = "",
    val description: String = "",
    val createdAt: Instant? = null,
    val location: com.example.spotly.domain.model.LocationPoint? = null
)
