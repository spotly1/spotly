package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import com.example.spotly.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _username = MutableStateFlow<String?>(null)

    val username: StateFlow<String?> = _username

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Completá todos los campos."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Ingresá un correo electrónico válido."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        repository.login(
            email = email,
            password = password,
            onSuccess = {
                _isLoading.value = false
                onSuccess()
            },
            onError = { error ->
                _isLoading.value = false
                _errorMessage.value = error
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
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _errorMessage.value = "Completá todos los campos."
            return
        }

        if (username.trim().length < 3) {
            _errorMessage.value = "El nombre de usuario debe tener al menos 3 caracteres."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Ingresá un correo electrónico válido."
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres."
            return
        }

        if (password != confirmPassword) {
            _errorMessage.value = "Las contraseñas no coinciden."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        repository.register(
            username = username.trim().lowercase(),
            email = email,
            password = password,
            onSuccess = {
                _isLoading.value = false
                onSuccess()
            },
            onError = { error ->
                _isLoading.value = false
                _errorMessage.value = error
            }
        )
    }

    fun logout() {
        repository.logout()
    }

    fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }

    fun loadCurrentUsername() {
        repository.getCurrentUsername(
            onSuccess = { username ->
                _username.value = username
            },
            onError = { error ->
                _errorMessage.value = error
            }
        )
    }
}