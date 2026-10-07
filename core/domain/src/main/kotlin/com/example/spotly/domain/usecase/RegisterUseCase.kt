package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository, private val emailValidator: EmailValidator) {
    operator fun invoke(
        username: String, email: String, password: String, confirmPassword: String,
        onResult: (AppResult<Unit>) -> Unit
    ) {
        val error = when {
            username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> AppError.RequiredFields
            username.trim().length < 3 -> AppError.UsernameTooShort
            !emailValidator.isValid(email) -> AppError.InvalidEmail
            password.length < 6 -> AppError.PasswordTooShort
            password != confirmPassword -> AppError.PasswordsDoNotMatch
            else -> null
        }
        if (error != null) { onResult(AppResult.Error(error)); return }
        repository.register(username.trim().lowercase(), email.trim(), password, onResult)
    }
}
