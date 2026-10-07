package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.User
import com.example.spotly.domain.repository.ProfileRepository

class GetCurrentProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(onResult: (AppResult<User>) -> Unit) =
        repository.getCurrentUserProfile(onResult)
}
