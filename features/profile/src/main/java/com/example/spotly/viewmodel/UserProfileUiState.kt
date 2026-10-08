package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.User

sealed interface UserProfileUiState {
    data object Loading : UserProfileUiState

    data class Error(
        val error: AppError
    ) : UserProfileUiState

    data class Success(
        val user: User,
        val postsState: ProfilePostsState = ProfilePostsState.Loading
    ) : UserProfileUiState
}