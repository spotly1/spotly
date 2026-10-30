
package com.example.spotly.domain.repository

import kotlinx.coroutines.flow.Flow

interface RecentSearchRepository {

    fun observeRecentUsers(userId: String): Flow<List<String>>

    suspend fun addRecentUser(
        userId: String,
        visitedUserId: String
    )

    suspend fun removeRecentUser(
        userId: String,
        visitedUserId: String
    )

    suspend fun clearRecentUsers(userId: String)
}
