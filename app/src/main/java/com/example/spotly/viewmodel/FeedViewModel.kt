package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.data.model.AppError
import com.example.spotly.data.model.Post
import com.example.spotly.data.repository.PostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class FeedUiState(val posts: List<Post> = emptyList(), val isLoading: Boolean = true,
                       val error: AppError? = null)

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel : ViewModel() {
    private val repository = PostRepository()
    private val reload = MutableStateFlow(0)
    val uiState = reload.flatMapLatest {
        repository.observeFeed().map { result ->
            result.fold(
                onSuccess = { FeedUiState(posts = it, isLoading = false) },
                onFailure = { FeedUiState(isLoading = false, error = AppError.FeedLoadFailed) }
            )
        }.onStart { emit(FeedUiState()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeedUiState())

    fun retry() { reload.value++ }
}
