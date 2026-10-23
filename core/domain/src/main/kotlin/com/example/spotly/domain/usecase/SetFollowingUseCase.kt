package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.repository.AuthRepository
import com.example.spotly.domain.repository.FollowRepository

class SetFollowingUseCase(private val repository: FollowRepository, private val auth: AuthRepository) {
    suspend operator fun invoke(uid: String, following: Boolean): AppResult<Unit> {
        val current = auth.currentUserId() ?: return AppResult.Error(AppError.Unauthenticated)
        if (uid.isBlank() || uid == current) return AppResult.Error(AppError.FollowUpdateFailed)
        return repository.setFollowing(uid, following)
    }
}
