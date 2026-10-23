package com.example.spotly.notifications

import android.content.Intent
import com.example.spotly.domain.model.NotificationType

data class NotificationTarget(val accountId: String, val noticeId: String,
    val actorId: String, val postId: String, val type: NotificationType) {
    companion object {
        const val ACTION = "com.example.spotly.OPEN_NOTIFICATION"
        fun from(intent: Intent?): NotificationTarget? {
            if (intent?.action != ACTION) return null
            val account = intent.getStringExtra("accountId") ?: return null
            val id = intent.getStringExtra("noticeId") ?: return null
            val actor = intent.getStringExtra("actorId") ?: return null
            val post = intent.getStringExtra("postId").orEmpty()
            val type = NotificationType.entries.firstOrNull { it.name == intent.getStringExtra("type") } ?: return null
            if (listOf(account, id, actor).any { it.isBlank() || it.length > 512 || '/' in it }) return null
            if (type == NotificationType.Like && (post.isBlank() || post.length > 512 || '/' in post)) return null
            return NotificationTarget(account, id, actor, post, type)
        }
    }
}
