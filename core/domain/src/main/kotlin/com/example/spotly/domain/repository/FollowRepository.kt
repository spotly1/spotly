package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.FollowSummary
import com.example.spotly.domain.model.FollowListType
import com.example.spotly.domain.model.User
import kotlinx.coroutines.flow.Flow

interface FollowRepository {
    fun observeUsers(uid: String, type: FollowListType): Flow<AppResult<List<User>>>
    fun observeFollowing(uid: String): Flow<AppResult<Set<String>>>
    fun observeProfile(uid: String): Flow<AppResult<FollowSummary>>
    suspend fun setFollowing(uid: String, following: Boolean): AppResult<Unit>
}
