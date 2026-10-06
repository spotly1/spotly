package com.example.spotly.data.model

import com.google.firebase.Timestamp

data class Post(
    val id: String = "",
    val authorId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val imagePublicId: String = "",
    val description: String = "",
    val createdAt: Timestamp? = null,
    val location: com.google.firebase.firestore.GeoPoint? = null
)
