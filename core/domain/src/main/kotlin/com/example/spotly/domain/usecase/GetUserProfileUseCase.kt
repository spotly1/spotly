package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.User
import com.example.spotly.domain.repository.ProfileRepository

class GetUserProfileUseCase(
    private val repository: ProfileRepository
) {
    operator fun invoke(
        uid: String,
        onResult: (AppResult<User>) -> Unit
    ) {
        repository.getUserProfile(uid, onResult)
    }
}