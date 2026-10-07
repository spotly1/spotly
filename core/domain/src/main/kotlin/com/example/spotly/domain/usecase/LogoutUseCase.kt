package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.AuthRepository

class LogoutUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.logout()
}
