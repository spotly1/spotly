package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.Post

sealed interface PostDetailUiState {
    data object Loading : PostDetailUiState
    data class Error(val error: AppError) : PostDetailUiState
    data object Deleted : PostDetailUiState
    data class Content(
        val post: Post,
        val isOwner: Boolean,
        val isEditing: Boolean = false,
        val description: String = post.description,
        val action: PostAction = PostAction.None,
        val error: AppError? = null,
        val saved: Boolean = false
    ) : PostDetailUiState
}

enum class PostAction { None, Saving, Deleting }
