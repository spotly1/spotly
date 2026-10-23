package com.example.spotly.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.domain.model.FollowListType
import com.example.spotly.viewmodel.FollowListViewModel

@Composable
fun FollowListRoute(type: FollowListType, viewModel: FollowListViewModel, onBack: () -> Unit, onUserClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FollowListScreen(type, state, onBack, onUserClick, viewModel::retry)
}
