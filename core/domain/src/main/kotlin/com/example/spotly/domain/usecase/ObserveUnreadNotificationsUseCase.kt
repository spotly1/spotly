package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.NotificationRepository

class ObserveUnreadNotificationsUseCase(private val repository: NotificationRepository) {
    operator fun invoke() = repository.observeUnread()
}
