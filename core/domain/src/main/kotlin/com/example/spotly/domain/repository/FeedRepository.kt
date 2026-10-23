package com.example.spotly.domain.repository
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.FeedPage
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun observe(): Flow<AppResult<FeedPage>>
}
