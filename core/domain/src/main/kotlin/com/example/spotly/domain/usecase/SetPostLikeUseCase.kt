package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.repository.AuthRepository
import com.example.spotly.domain.repository.LikeRepository

class SetPostLikeUseCase(private val repository: LikeRepository, private val auth: AuthRepository) {
    suspend operator fun invoke(postId: String, liked: Boolean): AppResult<Unit> {
        if (auth.currentUserId() == null) return AppResult.Error(AppError.Unauthenticated)
        if (postId.isBlank()) return AppResult.Error(AppError.PostNotFound)
        return repository.setLiked(postId, liked)
    }
}
