
package com.example.spotly.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.User
import com.example.spotly.domain.repository.RecentSearchRepository
import com.example.spotly.domain.usecase.GetCurrentUserIdUseCase
import com.example.spotly.domain.usecase.GetUserProfileUseCase
import com.example.spotly.domain.usecase.SearchUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchUsers: SearchUsersUseCase,
    private val savedState: SavedStateHandle,
    private val recentSearches: RecentSearchRepository,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val getUserProfile: GetUserProfileUseCase
) : ViewModel() {

    private val state = MutableStateFlow(
        SearchUiState(
            query = savedState.get<String>("query").orEmpty().take(30)
        )
    )

    val uiState = state.asStateFlow()

    private val _recentUsers = MutableStateFlow<List<User>>(emptyList())
    val recentUsers = _recentUsers.asStateFlow()

    private var request: Job? = null
    private var recentGeneration = 0

    init {
        search()
        observeRecentUsers()
    }

    fun changeQuery(query: String) {
        if (query.length > 30 || query == state.value.query) return

        savedState["query"] = query
        state.value = SearchUiState(query)
        search()
    }

    fun retry() {
        if (state.value.result is UserSearchState.Error) search()
    }

    private fun search() {
        request?.cancel()

        val query = state.value.query

        if (query.trim().length < 2) {
            state.value = SearchUiState(query)
            return
        }

        state.value = SearchUiState(query, UserSearchState.Loading)

        request = viewModelScope.launch {
            delay(300)

            val result = searchUsers(query)
            ensureActive()

            state.value = SearchUiState(
                query,
                when (result) {
                    is AppResult.Success ->
                        UserSearchState.Success(result.data)

                    is AppResult.Error ->
                        UserSearchState.Error(result.error)
                }
            )
        }
    }

    private fun observeRecentUsers() {
        val userId = getCurrentUserId() ?: return

        viewModelScope.launch {
            recentSearches.observeRecentUsers(userId).collect { ids ->

                val generation = ++recentGeneration
                val loadedUsers = mutableMapOf<String, User>()

                _recentUsers.value = emptyList()

                ids.forEach { id ->
                    getUserProfile(id) { result ->

                        if (
                            generation == recentGeneration &&
                            getCurrentUserId() == userId &&
                            result is AppResult.Success
                        ) {
                            loadedUsers[id] = result.data

                            _recentUsers.value = ids.mapNotNull {
                                loadedUsers[it]
                            }
                        }
                    }
                }
            }
        }
    }

    fun recordVisitedUser(visitedUserId: String) {
        val userId = getCurrentUserId() ?: return

        viewModelScope.launch {
            recentSearches.addRecentUser(userId, visitedUserId)
        }
    }

    fun removeRecentUser(visitedUserId: String) {
        val userId = getCurrentUserId() ?: return

        viewModelScope.launch {
            recentSearches.removeRecentUser(userId, visitedUserId)
        }
    }

    fun clearRecentUsers() {
        val userId = getCurrentUserId() ?: return

        viewModelScope.launch {
            recentSearches.clearRecentUsers(userId)
        }
    }
}
