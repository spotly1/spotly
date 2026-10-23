package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.*
import com.example.spotly.domain.usecase.ObserveFollowUsersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

sealed interface FollowListUiState {
    data object Loading : FollowListUiState
    data class Success(val users: List<User>) : FollowListUiState
    data class Error(val error: AppError) : FollowListUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class FollowListViewModel(uid: String, type: FollowListType, observeUsers: ObserveFollowUsersUseCase) : ViewModel() {
    private val reload = MutableStateFlow(0)
    val uiState: StateFlow<FollowListUiState> = reload.flatMapLatest {
        observeUsers(uid, type).map { result ->
            when (result) {
                is AppResult.Success -> FollowListUiState.Success(result.data)
                is AppResult.Error -> FollowListUiState.Error(result.error)
            }
        }.onStart { emit(FollowListUiState.Loading) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 0), FollowListUiState.Loading)

    fun retry() { reload.value++ }
}
