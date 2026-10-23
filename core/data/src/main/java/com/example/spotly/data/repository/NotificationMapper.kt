package com.example.spotly.data.repository

import com.example.spotly.domain.model.AppNotification
import com.example.spotly.domain.model.NotificationType
import com.example.spotly.network.NotificationDto
import com.example.spotly.network.UserDto
import com.google.firebase.firestore.FieldValue
import java.time.Instant

internal fun NotificationDto.toDomain(id: String, actor: UserDto): AppNotification? {
    val kind = when (type) {
        "follow" -> NotificationType.Follow
        "like" -> NotificationType.Like
        else -> return null
    }
    return AppNotification(id, actorId, actor.username, actor.profileImageUrl, kind, postId,
        createdAt?.let { Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong()) }, read)
}

internal fun notificationData(actorId: String, recipientId: String, type: String, postId: String = "") = mapOf(
    "actorId" to actorId, "recipientId" to recipientId, "type" to type, "postId" to postId,
    "createdAt" to FieldValue.serverTimestamp(), "read" to false
)
