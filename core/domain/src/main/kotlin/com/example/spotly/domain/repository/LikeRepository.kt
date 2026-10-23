package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.PostLikes
import com.example.spotly.domain.model.User
import kotlinx.coroutines.flow.Flow

interface LikeRepository {
    fun observeUsers(postId: String): Flow<AppResult<List<User>>>
    fun observe(postId: String): Flow<AppResult<PostLikes>>
    suspend fun setLiked(postId: String, liked: Boolean): AppResult<Unit>
}
