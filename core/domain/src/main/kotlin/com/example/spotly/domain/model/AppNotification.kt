package com.example.spotly.domain.model

import java.time.Instant

enum class NotificationType { Follow, Like }

data class AppNotification(
    val id: String,
    val actorId: String,
    val username: String,
    val profileImageUrl: String,
    val type: NotificationType,
    val postId: String,
    val createdAt: Instant?,
    val read: Boolean,
    val postImageUrl: String = ""
)
