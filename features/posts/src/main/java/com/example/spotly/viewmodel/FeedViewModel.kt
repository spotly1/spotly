package com.example.spotly.viewmodel

import com.example.spotly.domain.usecase.ObserveFeedUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.AppResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(private val observeFeed: ObserveFeedUseCase) : ViewModel() {
    private val reload = MutableStateFlow(0)
    val uiState: StateFlow<FeedUiState> = reload.flatMapLatest {
        observeFeed().map { result ->
            when (result) {
                is AppResult.Success -> FeedUiState.Success(result.data)
                is AppResult.Error -> FeedUiState.Error(result.error)
            }
        }.onStart { emit(FeedUiState.Loading) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeedUiState.Loading)

    fun retry() { reload.value++ }
}
