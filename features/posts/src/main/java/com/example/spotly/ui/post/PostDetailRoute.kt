package com.example.spotly.ui.post

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.spotly.viewmodel.PostDetailUiState
import com.example.spotly.viewmodel.PostDetailViewModel

@Composable
fun PostDetailRoute(viewModel: PostDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnBack by rememberUpdatedState(onBack)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.uiState.collect {
                if (it == PostDetailUiState.Deleted && viewModel.onDeletedHandled()) currentOnBack()
            }
        }
    }
    PostDetailScreen(state, onBack, viewModel::retry, viewModel::edit,
        viewModel::changeDescription, viewModel::cancelEdit, viewModel::save, viewModel::delete)
}
