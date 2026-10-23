package com.example.spotly.notifications

import com.example.spotly.domain.model.AppNotification
import java.time.Instant

/** La primera respuesta es una línea de base, no una tanda de avisos nuevos. */
internal class FreshNotificationTracker {
    private var initialized = false
    private var watermark: Instant? = null
    private val seen = LinkedHashSet<String>()

    fun accept(items: List<AppNotification>): List<AppNotification> {
        val fresh = items.filter { notice ->
            val time = notice.createdAt
            initialized && !notice.read && time != null &&
                (watermark == null || !time.isBefore(watermark)) && key(notice) !in seen
        }
        items.forEach { seen.add(key(it)) }
        while (seen.size > 200) seen.remove(seen.first())
        items.mapNotNull { it.createdAt }.maxOrNull()?.let { latest ->
            if (watermark == null || latest.isAfter(watermark)) watermark = latest
        }
        initialized = true
        return fresh.sortedBy { it.createdAt }
    }

    private fun key(notice: AppNotification) = notice.id + ":" + notice.createdAt
}
