package com.example.spotly.ui.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.LikeListViewModel

@Composable
fun LikeListRoute(viewModel: LikeListViewModel, onBack: () -> Unit, onUserClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LikeListScreen(state, onBack, onUserClick, viewModel::retry)
}
