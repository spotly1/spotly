package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.repository.AuthRepository
import com.example.spotly.domain.repository.PostDetailRepository

class DeletePostUseCase(private val posts: PostDetailRepository, private val auth: AuthRepository) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        val uid = auth.currentUserId() ?: return AppResult.Error(AppError.Unauthenticated)
        if (id.isBlank() || '/' in id) return AppResult.Error(AppError.PostNotFound)
        return posts.delete(id, uid)
    }
}
