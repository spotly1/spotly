package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.LikeRepository

class ObservePostLikeUsersUseCase(private val repository: LikeRepository) {
    operator fun invoke(postId: String) = repository.observeUsers(postId)
}
