
package com.example.spotly.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.spotly.domain.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.recentSearchDataStore by preferencesDataStore(
    name = "recent_searches"
)

class DataStoreRecentSearchRepository(
    context: Context
) : RecentSearchRepository {

    private val dataStore = context.applicationContext.recentSearchDataStore

    private fun key(userId: String) =
        stringPreferencesKey("recent_users_$userId")

    private fun decode(value: String): List<String> =
        value.split(",").filter { it.isNotBlank() }

    override fun observeRecentUsers(userId: String): Flow<List<String>> {
        return dataStore.data.map { preferences ->
            decode(preferences[key(userId)].orEmpty())
        }
    }

    override suspend fun addRecentUser(
        userId: String,
        visitedUserId: String
    ) {
        if (
            userId.isBlank() ||
            visitedUserId.isBlank() ||
            userId == visitedUserId
        ) return

        dataStore.edit { preferences ->
            val recent = decode(
                preferences[key(userId)].orEmpty()
            )

            val updated = (
                    listOf(visitedUserId) +
                            recent.filter { it != visitedUserId }
                    ).take(5)

            preferences[key(userId)] = updated.joinToString(",")
        }
    }

    override suspend fun removeRecentUser(
        userId: String,
        visitedUserId: String
    ) {
        if (userId.isBlank()) return

        dataStore.edit { preferences ->
            val recent = decode(
                preferences[key(userId)].orEmpty()
            )

            preferences[key(userId)] = recent
                .filter { it != visitedUserId }
                .joinToString(",")
        }
    }

    override suspend fun clearRecentUsers(userId: String) {
        if (userId.isBlank()) return

        dataStore.edit { preferences ->
            preferences.remove(key(userId))
        }
    }
}
