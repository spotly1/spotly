package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.AuthRepository

class GetCurrentUserIdUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): String? = repository.currentUserId()
}