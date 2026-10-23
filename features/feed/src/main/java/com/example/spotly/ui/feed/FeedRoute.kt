package com.example.spotly.ui.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.FeedViewModel

@Composable
fun FeedRoute(
    viewModel: FeedViewModel,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onCreatePostClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLikesClick: (String) -> Unit,
    onNotificationsClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val unread by viewModel.unreadNotifications.collectAsStateWithLifecycle()

    FeedScreen(
        state = state,
        onRetry = viewModel::retry,
        onUserClick = onUserClick,
        onPostClick = onPostClick,
        onCreatePostClick = onCreatePostClick,
        onSearchClick = onSearchClick,
        onLike = viewModel::setLiked,
        onRetryLikes = viewModel::retryLikes,
        onLikesClick = onLikesClick,
        unreadNotifications = unread,
        onNotificationsClick = onNotificationsClick
    )
}
