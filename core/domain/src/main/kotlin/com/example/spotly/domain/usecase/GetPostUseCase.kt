package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.Post
import com.example.spotly.domain.repository.PostDetailRepository

class GetPostUseCase(private val posts: PostDetailRepository) {
    suspend operator fun invoke(id: String): AppResult<Post> =
        if (id.isBlank() || '/' in id) AppResult.Error(AppError.PostNotFound) else posts.get(id)
}
