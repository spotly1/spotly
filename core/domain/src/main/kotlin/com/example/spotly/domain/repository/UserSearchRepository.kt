package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.User

interface UserSearchRepository {
    suspend fun search(prefix: String): AppResult<List<User>>
}
