package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.LikeRepository

class ObservePostLikesUseCase(private val repository: LikeRepository) {
    operator fun invoke(postId: String) = repository.observe(postId)
}
