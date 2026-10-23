package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.*
import kotlinx.coroutines.flow.flowOf

class ObserveFollowingIdsUseCase(private val follows: FollowRepository, private val auth: AuthRepository) {
    operator fun invoke() = auth.currentUserId()?.let(follows::observeFollowing)
        ?: flowOf(AppResult.Error(AppError.Unauthenticated))
}
