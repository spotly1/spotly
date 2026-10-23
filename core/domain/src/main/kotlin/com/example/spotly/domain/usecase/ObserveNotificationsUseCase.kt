package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.NotificationRepository

class ObserveNotificationsUseCase(private val repository: NotificationRepository) {
    operator fun invoke(onlyUnread: Boolean) = repository.observe(onlyUnread)
}
