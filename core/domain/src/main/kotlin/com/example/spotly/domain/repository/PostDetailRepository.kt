package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.Post

interface PostDetailRepository {
    suspend fun get(id: String): AppResult<Post>
    suspend fun updateDescription(id: String, uid: String, description: String): AppResult<Unit>
    suspend fun delete(id: String, uid: String): AppResult<Unit>
}
