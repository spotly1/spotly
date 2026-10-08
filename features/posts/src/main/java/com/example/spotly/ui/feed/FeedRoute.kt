package com.example.spotly.ui.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.FeedViewModel

@Composable
fun FeedRoute(
    viewModel: FeedViewModel,
    onUserClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FeedScreen(
        state = state,
        onRetry = viewModel::retry,
        onUserClick = onUserClick
    )
}
