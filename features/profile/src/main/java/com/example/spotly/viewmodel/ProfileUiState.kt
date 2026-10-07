package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.Post
import com.example.spotly.domain.model.User

sealed interface ProfileSaveState {
    data object Idle : ProfileSaveState
    data object Saving : ProfileSaveState
    data object Saved : ProfileSaveState
    data class Error(val error: AppError) : ProfileSaveState
}

sealed interface ProfilePostsState {
    data object Loading : ProfilePostsState
    data class Success(val posts: List<Post> = emptyList()) : ProfilePostsState
    data class Error(val error: AppError) : ProfilePostsState
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Error(override val error: AppError) : ProfileUiState
    data class Success(
        override val user: User,
        val postsState: ProfilePostsState = ProfilePostsState.Loading,
        val saveState: ProfileSaveState = ProfileSaveState.Idle
    ) : ProfileUiState {
        override val error: AppError? get() = (saveState as? ProfileSaveState.Error)?.error
    }

    val user: User? get() = null
    val error: AppError? get() = null
    val isLoading: Boolean get() = this is Loading ||
        (this is Success && saveState is ProfileSaveState.Saving)
    val posts: List<Post> get() = when (this) {
        is Success -> (postsState as? ProfilePostsState.Success)?.posts.orEmpty()
        is Error, Loading -> emptyList()
    }
    val postsLoading: Boolean get() = this is Loading ||
        (this is Success && postsState is ProfilePostsState.Loading)
    val postsError: Boolean get() = this is Error ||
        (this is Success && postsState is ProfilePostsState.Error)
}
