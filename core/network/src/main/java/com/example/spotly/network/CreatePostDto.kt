package com.example.spotly.network

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint

// La fecha se calcula en Firestore, no con el reloj del dispositivo.
data class CreatePostDto(
    val authorId: String,
    val imageUrl: String,
    val imagePublicId: String,
    val description: String,
    val location: GeoPoint?,
    val createdAt: FieldValue = FieldValue.serverTimestamp()
)
