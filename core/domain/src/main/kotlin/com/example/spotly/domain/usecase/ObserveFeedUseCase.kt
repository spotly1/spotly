package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.PostRepository

class ObserveFeedUseCase(private val repository: PostRepository) {
    operator fun invoke() = repository.observeFeed()
}
