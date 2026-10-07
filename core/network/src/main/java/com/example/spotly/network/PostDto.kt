package com.example.spotly.network

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

/** Forma del documento remoto: los tipos de Firebase no salen al dominio. */
data class PostDto(
    val authorId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val imagePublicId: String = "",
    val description: String = "",
    val createdAt: Timestamp? = null,
    val location: GeoPoint? = null
)
