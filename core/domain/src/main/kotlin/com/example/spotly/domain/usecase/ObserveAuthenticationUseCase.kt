package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.AuthRepository

class ObserveAuthenticationUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.observeAuthentication()
}
