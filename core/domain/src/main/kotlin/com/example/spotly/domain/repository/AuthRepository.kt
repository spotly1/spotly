package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun currentUserId(): String?
    fun login(email: String, password: String, onResult: (AppResult<Unit>) -> Unit)
    fun register(username: String, email: String, password: String, onResult: (AppResult<Unit>) -> Unit)
    fun logout()
    fun observeAuthentication(): Flow<Boolean>
}
