package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository, private val emailValidator: EmailValidator) {
    operator fun invoke(email: String, password: String, onResult: (AppResult<Unit>) -> Unit) {
        when {
            email.isBlank() || password.isBlank() -> onResult(AppResult.Error(AppError.RequiredFields))
            !emailValidator.isValid(email) -> onResult(AppResult.Error(AppError.InvalidEmail))
            else -> repository.login(email.trim(), password, onResult)
        }
    }
}
