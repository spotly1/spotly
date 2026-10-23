package com.example.spotly.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.spotly.MainActivity
import com.example.spotly.R
import com.example.spotly.core.ui.R as UiR
import com.example.spotly.domain.model.*
import com.example.spotly.domain.usecase.*

class LocalNotificationController(
    context: Context,
    private val observe: ObserveLocalNotificationsUseCase,
    private val currentUid: GetCurrentUserIdUseCase,
    private val markRead: MarkNotificationReadUseCase
) {
    private val context = context.applicationContext
    private val manager = this.context.getSystemService(NotificationManager::class.java)

    init {
        manager.createNotificationChannel(NotificationChannel(CHANNEL,
            context.getString(UiR.string.local_notifications_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = context.getString(UiR.string.local_notifications_channel_description)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        })
        retainAccount(currentUid())
    }

    fun retainAccount(uid: String?) {
        manager.activeNotifications.filter {
            it.tag?.startsWith("spotly:") == true && (uid == null || !it.tag.startsWith("spotly:$uid:"))
        }.forEach { manager.cancel(it.tag, it.id) }
    }

    suspend fun observeWhileVisible(uid: String) {
        val tracker = FreshNotificationTracker()
        observe().collect { result ->
            if (currentUid() != uid) return@collect
            if (result is AppResult.Success) {
                result.data.filter { it.read }.forEach { manager.cancel(tag(uid, it.id), 0) }
                tracker.accept(result.data).forEach { show(uid, it) }
            }
        }
    }

    suspend fun opened(target: NotificationTarget) {
        if (currentUid() == target.accountId) {
            manager.cancel(tag(target.accountId, target.noticeId), 0)
            markRead(target.noticeId) // Si no hay conexión permanece sin leer en la campanita.
        }
    }

    private fun show(uid: String, notice: AppNotification) {
        if (currentUid() != uid || !manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val intent = Intent(context, MainActivity::class.java).apply {
            action = NotificationTarget.ACTION
            data = Uri.Builder().scheme("spotly").authority("notification")
                .appendPath(uid).appendPath(notice.id).appendPath(notice.createdAt.toString()).build()
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("accountId", uid)
            putExtra("noticeId", notice.id)
            putExtra("actorId", notice.actorId)
            putExtra("postId", notice.postId)
            putExtra("type", notice.type.name)
        }
        val pending = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val name = notice.username.ifBlank { context.getString(UiR.string.notification_unknown_user) }
        val body = context.getString(if (notice.type == NotificationType.Like)
            UiR.string.notification_like else UiR.string.notification_follow, name)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(UiR.string.local_notifications_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .build()
        try { manager.notify(tag(uid, notice.id), 0, notification) }
        catch (_: SecurityException) { /* El usuario puede revocar el permiso mientras se publica. */ }
    }

    private fun tag(uid: String, id: String) = "spotly:$uid:$id"
    companion object { const val CHANNEL = "spotly_social" }
}
