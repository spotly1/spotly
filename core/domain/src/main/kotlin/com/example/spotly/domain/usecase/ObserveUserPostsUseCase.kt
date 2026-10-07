package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.PostRepository

class ObserveUserPostsUseCase(private val repository: PostRepository) {
    operator fun invoke(uid: String) = repository.observeUserPosts(uid)
}
