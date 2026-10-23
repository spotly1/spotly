package com.example.spotly.ui.notifications

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.spotly.domain.model.NotificationType
import com.example.spotly.viewmodel.NotificationsViewModel

@Composable
fun NotificationsRoute(viewModel: NotificationsViewModel, onBack: () -> Unit,
    onUserClick: (String) -> Unit, onPostClick: (String) -> Unit, onSettings: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val userClick by rememberUpdatedState(onUserClick)
    val postClick by rememberUpdatedState(onPostClick)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.uiState.collect { current ->
                if (current.destination != null) viewModel.consumeDestination()?.let {
                    if (it.type == NotificationType.Follow) userClick(it.actorId) else postClick(it.postId)
                }
            }
        }
    }
    NotificationsScreen(state, onBack, viewModel::retry, viewModel::filterUnread, viewModel::open, onSettings)
}
