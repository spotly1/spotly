package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.*
import com.example.spotly.domain.usecase.*
import com.example.spotly.ui.state.PostLikesController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModel(
    private val getPage: GetExplorePageUseCase,
    observeFollowing: ObserveFollowingIdsUseCase,
    observeLikes: ObservePostLikesUseCase,
    setLike: SetPostLikeUseCase,
    observeUnread: ObserveUnreadNotificationsUseCase
) : ViewModel() {
    private val reload = MutableStateFlow(0)
    private val nextPage = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val likes = PostLikesController(viewModelScope, observeLikes, setLike)
    val unreadNotifications = reload.flatMapLatest { observeUnread() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 0), null)

    val uiState: StateFlow<FeedUiState> = combine(reload, observeFollowing()) { _, result -> result }
        .flatMapLatest { result ->
            when (result) {
                is AppResult.Error -> flowOf<FeedUiState>(FeedUiState.Error(result.error))
                is AppResult.Success -> pages(result.data)
            }
        }.onStart { emit(FeedUiState.Loading) }
        .flatMapLatest { state ->
            if (state is FeedUiState.Success)
                likes.observe(state.posts.map { it.id }).map { state.copy(likes = it) }
            else flowOf(state)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0, 0), FeedUiState.Loading)

    private fun pages(following: Set<String>): Flow<FeedUiState> = flow {
        var cursor: ExploreCursor? = null
        var posts = emptyList<Post>()
        var finished = false
        emit(FeedUiState.Loading)
        nextPage.onStart { emit(Unit) }.collect {
            if (finished) return@collect
            if (posts.isNotEmpty()) emit(FeedUiState.Success(posts, hasMore = true, loadingMore = true))
            when (val result = getPage(following, cursor)) {
                is AppResult.Error -> {
                    if (posts.isEmpty()) emit(FeedUiState.Error(result.error))
                    else emit(FeedUiState.Success(posts, hasMore = true, pageError = result.error))
                }
                is AppResult.Success -> {
                    posts = (posts + result.data.posts).distinctBy { it.id }
                    cursor = result.data.next
                    finished = cursor == null
                    emit(FeedUiState.Success(posts, hasMore = !finished))
                }
            }
        }
    }

    fun refresh() { reload.value++ }
    fun loadMore() {
        val state = uiState.value as? FeedUiState.Success ?: return
        if (state.hasMore && !state.loadingMore) nextPage.tryEmit(Unit)
    }
    fun retryLikes(id: String) = likes.retry(id)
    fun setLiked(id: String, liked: Boolean) {
        val current = (uiState.value as? FeedUiState.Success)?.likes?.get(id) ?: return
        val summary = current.summary ?: return
        if (!current.busy && summary.likedByMe != liked) likes.set(id, liked)
    }
}
