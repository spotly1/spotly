package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppNotification

sealed interface NotificationsContent {
    data object Loading : NotificationsContent
    data class Success(val items: List<AppNotification>) : NotificationsContent
    data class Error(val error: AppError) : NotificationsContent
}

data class NotificationsUiState(
    val content: NotificationsContent = NotificationsContent.Loading,
    val onlyUnread: Boolean = false,
    val openingId: String? = null,
    val actionError: AppError? = null,
    val destination: AppNotification? = null
)
