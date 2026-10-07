package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.LocationPoint

sealed interface CreatePostUiState {
    data class Editing(
        override val location: LocationPoint? = null,
        override val error: AppError? = null,
        override val locationError: Boolean = false
    ) : CreatePostUiState
    data class Locating(override val location: LocationPoint?) : CreatePostUiState
    data class Publishing(override val location: LocationPoint?) : CreatePostUiState
    data object Published : CreatePostUiState

    val location: LocationPoint? get() = null
    val error: AppError? get() = null
    val locationError: Boolean get() = false
    val isPublishing: Boolean get() = this is Publishing
    val published: Boolean get() = this is Published
    val locating: Boolean get() = this is Locating
}
