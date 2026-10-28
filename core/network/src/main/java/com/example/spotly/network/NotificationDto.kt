package com.example.spotly.network

import com.google.firebase.Timestamp

data class NotificationDto(
    val recipientId: String = "",
    val actorId: String = "",
    val type: String = "",
    val postId: String = "",
    val postImageUrl: String = "",
    val createdAt: Timestamp? = null,
    val read: Boolean = false
)
