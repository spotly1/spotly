package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.User

interface ProfileRepository {
    fun getCurrentUserProfile(onResult: (AppResult<User>) -> Unit)
    fun updateProfile(
        description: String, profileImageUrl: String, profileImagePublicId: String,
        onResult: (AppResult<Unit>) -> Unit
    )
}
