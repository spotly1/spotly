package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.Post
import com.example.spotly.ui.state.LikeUiState

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Success(val posts: List<Post> = emptyList(), val hasFollowing: Boolean = true,
        val likes: Map<String, LikeUiState> = emptyMap(),
        val isCached: Boolean = false, val refreshError: AppError? = null,
        val hasMore: Boolean = false, val loadingMore: Boolean = false,
        val pageError: AppError? = null) : FeedUiState
    data class Error(val error: AppError) : FeedUiState
}
