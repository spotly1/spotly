package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppNotification
import com.example.spotly.domain.model.AppResult
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    /** Para avisos del sistema: omite snapshots locales y escrituras aún no confirmadas. */
    fun observeForAlerts(): Flow<AppResult<List<AppNotification>>> = observe(false)
    fun observe(onlyUnread: Boolean): Flow<AppResult<List<AppNotification>>>
    fun observeUnread(): Flow<AppResult<Boolean>>
    suspend fun markRead(id: String): AppResult<Unit>
}
