package com.example.spotly.ui.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.FeedViewModel

@Composable
fun FeedRoute(viewModel: FeedViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeedScreen(state, viewModel::retry)
}
