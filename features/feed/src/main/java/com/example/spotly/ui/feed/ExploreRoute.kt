package com.example.spotly.ui.feed

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.ExploreViewModel

@Composable
fun ExploreRoute(viewModel: ExploreViewModel, onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit, onCreatePostClick: () -> Unit, onSearchClick: () -> Unit,
    onLikesClick: (String) -> Unit, onNotificationsClick: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val unread by viewModel.unreadNotifications.collectAsStateWithLifecycle()
    FeedScreen(state, viewModel::refresh, onUserClick, onPostClick, onCreatePostClick,
        onSearchClick, viewModel::setLiked, viewModel::retryLikes, onLikesClick, unread,
        onNotificationsClick, explore = true, onLoadMore = viewModel::loadMore)
}
