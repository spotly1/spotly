package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.usecase.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val observeAuthentication: ObserveAuthenticationUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase
) : ViewModel() {
    private val state = MutableStateFlow<AuthUiState>(AuthUiState.Initializing)
    val uiState: StateFlow<AuthUiState> = state.asStateFlow()
    private var registrationInProgress = false

    init {
        viewModelScope.launch {
            observeAuthentication().collect { authenticated ->
                if (registrationInProgress && authenticated) return@collect
                if (authenticated) state.value = AuthUiState.Authenticated
                else when (state.value) {
                    AuthUiState.Initializing, AuthUiState.Authenticated -> state.value = AuthUiState.Idle
                    AuthUiState.Idle, AuthUiState.Submitting, is AuthUiState.Error -> Unit
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (state.value.isLoading || state.value.isAuthenticated) return
        state.value = AuthUiState.Submitting
        loginUseCase(email, password) { result ->
            state.value = when (result) {
                is AppResult.Success -> AuthUiState.Authenticated
                is AppResult.Error -> AuthUiState.Error(result.error)
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        if (state.value.isLoading || state.value.isAuthenticated) return
        state.value = AuthUiState.Submitting
        registrationInProgress = true
        registerUseCase(username, email, password, confirmPassword) { result ->
            registrationInProgress = false
            state.value = when (result) {
                is AppResult.Success -> AuthUiState.Authenticated
                is AppResult.Error -> AuthUiState.Error(result.error)
            }
        }
    }

    fun logout() = logoutUseCase()

    fun currentUserId(): String? = getCurrentUserId()

    fun clearError() {
        if (state.value is AuthUiState.Error) state.value = AuthUiState.Idle
    }
}
