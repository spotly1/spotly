package com.example.spotly.domain.usecase
import com.example.spotly.domain.repository.NotificationRepository

class ObserveLocalNotificationsUseCase(private val repository: NotificationRepository) {
    operator fun invoke() = repository.observeForAlerts()
}
