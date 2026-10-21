package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.Post

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Success(val posts: List<Post> = emptyList()) : FeedUiState
    data class Error(val error: AppError) : FeedUiState
}
