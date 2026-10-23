package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.User

data class SearchUiState(val query: String = "", val result: UserSearchState = UserSearchState.Idle)

sealed interface UserSearchState {
    data object Idle : UserSearchState
    data object Loading : UserSearchState
    data class Success(val users: List<User>) : UserSearchState
    data class Error(val error: AppError) : UserSearchState
}
