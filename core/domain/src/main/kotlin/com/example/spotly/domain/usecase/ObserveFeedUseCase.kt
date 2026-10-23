package com.example.spotly.domain.usecase
import com.example.spotly.domain.repository.FeedRepository

class ObserveFeedUseCase(private val repository: FeedRepository) {
    operator fun invoke() = repository.observe()
}
