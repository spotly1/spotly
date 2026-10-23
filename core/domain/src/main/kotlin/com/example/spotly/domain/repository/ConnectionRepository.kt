package com.example.spotly.domain.repository
import kotlinx.coroutines.flow.Flow

interface ConnectionRepository {
    fun isOnline(): Boolean
    fun observe(): Flow<Boolean>
}
