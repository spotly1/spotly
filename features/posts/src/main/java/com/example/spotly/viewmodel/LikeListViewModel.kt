package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.*
import com.example.spotly.domain.usecase.ObservePostLikeUsersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

sealed interface LikeListUiState {
    data object Loading : LikeListUiState
    data class Success(val users: List<User>) : LikeListUiState
    data class Error(val error: AppError) : LikeListUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class LikeListViewModel(postId: String, observeUsers: ObservePostLikeUsersUseCase) : ViewModel() {
    private val reload = MutableStateFlow(0)
    val uiState: StateFlow<LikeListUiState> = reload.flatMapLatest {
        observeUsers(postId).map { result ->
            when (result) {
                is AppResult.Success -> LikeListUiState.Success(result.data)
                is AppResult.Error -> LikeListUiState.Error(result.error)
            }
        }.onStart { emit(LikeListUiState.Loading) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 0), LikeListUiState.Loading)

    fun retry() { reload.value++ }
}
