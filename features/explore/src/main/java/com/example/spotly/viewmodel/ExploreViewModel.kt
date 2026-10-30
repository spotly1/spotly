package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.ExploreCursor
import com.example.spotly.domain.model.Post
import com.example.spotly.domain.usecase.GetExplorePageUseCase
import com.example.spotly.domain.usecase.ObserveFollowingIdsUseCase
import com.example.spotly.domain.usecase.ObservePostLikesUseCase
import com.example.spotly.domain.usecase.ObserveUnreadNotificationsUseCase
import com.example.spotly.domain.usecase.SetPostLikeUseCase
import com.example.spotly.ui.state.PostLikesController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

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

    val uiState: StateFlow<ExploreUiState> = combine(
        reload,
        observeFollowing()
    ) { _, result -> result }
        .flatMapLatest { result ->
            when (result) {
                is AppResult.Error -> flowOf<ExploreUiState>(ExploreUiState.Error(result.error))
                is AppResult.Success -> pages(result.data)
            }
        }.onStart { emit(ExploreUiState.Loading) }
        .flatMapLatest { state ->
            if (state is ExploreUiState.Success)
                likes.observe(state.posts.map { it.id }).map { state.copy(likes = it) }
            else flowOf(state)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0, 0), ExploreUiState.Loading)

    private fun pages(following: Set<String>): Flow<ExploreUiState> = flow {
        var cursor: ExploreCursor? = null
        var posts = emptyList<Post>()
        var finished = false
        emit(ExploreUiState.Loading)
        nextPage.onStart { emit(Unit) }.collect {
            if (finished) return@collect
            if (posts.isNotEmpty()) emit(
                ExploreUiState.Success(
                    posts,
                    hasMore = true,
                    loadingMore = true
                )
            )
            when (val result = getPage(following, cursor)) {
                is AppResult.Error -> {
                    if (posts.isEmpty()) emit(ExploreUiState.Error(result.error))
                    else emit(ExploreUiState.Success(posts, hasMore = true, pageError = result.error))
                }

                is AppResult.Success -> {
                    posts = (posts + result.data.posts).distinctBy { it.id }
                    cursor = result.data.next
                    finished = cursor == null
                    emit(ExploreUiState.Success(posts, hasMore = !finished))
                }
            }
        }
    }

    fun refresh() { reload.value++ }
    fun loadMore() {
        val state = uiState.value as? ExploreUiState.Success ?: return
        if (state.hasMore && !state.loadingMore) nextPage.tryEmit(Unit)
    }
    fun retryLikes(id: String) = likes.retry(id)
    fun setLiked(id: String, liked: Boolean) {
        val current = (uiState.value as? ExploreUiState.Success)?.likes?.get(id) ?: return
        val summary = current.summary ?: return
        if (!current.busy && summary.likedByMe != liked) likes.set(id, liked)
    }
}