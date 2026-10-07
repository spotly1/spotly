package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError

sealed interface AuthUiState {
    data object Initializing : AuthUiState
    data object Idle : AuthUiState
    data object Submitting : AuthUiState
    data class Error(override val error: AppError) : AuthUiState
    data object Authenticated : AuthUiState

    val error: AppError? get() = null
    val isLoading: Boolean get() = this is Submitting
    val isSessionLoading: Boolean get() = this is Initializing
    val isAuthenticated: Boolean get() = this is Authenticated
}
