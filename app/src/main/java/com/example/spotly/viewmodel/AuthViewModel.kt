package com.example.spotly.viewmodel

import com.example.spotly.data.model.AppError

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isSessionLoading: Boolean = true,
    val isLoading: Boolean = false,
    val error: AppError? = null
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var registrationInProgress = false

    init {
        viewModelScope.launch {
            repository.observeAuthentication().collect { isAuthenticated ->
                if (registrationInProgress && isAuthenticated) return@collect
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = isAuthenticated,
                    isSessionLoading = false,
                    isLoading = false
                )
            }
        }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            setError(AppError.RequiredFields)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(AppError.InvalidEmail)
            return
        }

        setLoading()
        repository.login(
            email = email.trim(),
            password = password,
            onSuccess = onSuccess,
            onError = { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = error)
            }
        )
    }

    fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        when {
            username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                setError(AppError.RequiredFields)
                return
            }

            username.trim().length < 3 -> {
                setError(AppError.UsernameTooShort)
                return
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                setError(AppError.InvalidEmail)
                return
            }

            password.length < 6 -> {
                setError(AppError.PasswordTooShort)
                return
            }

            password != confirmPassword -> {
                setError(AppError.PasswordsDoNotMatch)
                return
            }
        }

        setLoading()
        registrationInProgress = true
        repository.register(
            username = username.trim().lowercase(),
            email = email.trim(),
            password = password,
            onSuccess = {
                registrationInProgress = false
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = true,
                    isLoading = false,
                    error = null
                )
                onSuccess()
            },
            onError = { error ->
                registrationInProgress = false
                _uiState.value = _uiState.value.copy(isLoading = false, error = error)
            }
        )
    }

    fun logout() = repository.logout()

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun setLoading() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
    }

    private fun setError(message: AppError) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = message)
    }
}
