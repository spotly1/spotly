package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.NotificationRepository

class MarkNotificationReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(id: String) = repository.markRead(id)
}
