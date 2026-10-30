
package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.Post
import com.example.spotly.ui.state.LikeUiState

sealed interface ExploreUiState {
    data object Loading : ExploreUiState

    data class Success(
        val posts: List<Post> = emptyList(),
        val likes: Map<String, LikeUiState> = emptyMap(),
        val hasMore: Boolean = false,
        val loadingMore: Boolean = false,
        val pageError: AppError? = null
    ) : ExploreUiState

    data class Error(val error: AppError) : ExploreUiState
}
