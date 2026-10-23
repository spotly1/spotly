package com.example.spotly.viewmodel

import com.example.spotly.domain.usecase.ObserveFeedUseCase
import com.example.spotly.domain.usecase.ObservePostLikesUseCase
import com.example.spotly.domain.usecase.SetPostLikeUseCase
import com.example.spotly.domain.usecase.ObserveUnreadNotificationsUseCase
import com.example.spotly.ui.state.PostLikesController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.AppResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(private val observeFeed: ObserveFeedUseCase,
    observeLikes: ObservePostLikesUseCase, setLike: SetPostLikeUseCase,
    observeUnread: ObserveUnreadNotificationsUseCase) : ViewModel() {
    private val likes = PostLikesController(viewModelScope, observeLikes, setLike)
    private val reload = MutableStateFlow(0)
    val unreadNotifications: StateFlow<AppResult<Boolean>?> = reload.flatMapLatest { observeUnread() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 0), null)
    val uiState: StateFlow<FeedUiState> = reload.flatMapLatest {
        observeFeed().map { result ->
            when (result) {
                is AppResult.Success -> FeedUiState.Success(result.data.posts, result.data.hasFollowing,
                    isCached = result.data.isCached, refreshError = result.data.refreshError)
                is AppResult.Error -> FeedUiState.Error(result.error)
            }
        }.onStart { emit(FeedUiState.Loading) }
    }.flatMapLatest { state ->
        if (state is FeedUiState.Success) likes.observe(state.posts.map { it.id }).map { state.copy(likes = it) }
        else flowOf(state)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 0), FeedUiState.Loading)

    fun setLiked(postId: String, liked: Boolean) {
        val current = (uiState.value as? FeedUiState.Success)?.likes?.get(postId) ?: return
        val summary = current.summary ?: return
        if (current.busy || summary.likedByMe == liked) return
        likes.set(postId, liked)
    }

    fun retryLikes(postId: String) = likes.retry(postId)

    fun retry() { reload.value++ }
}
