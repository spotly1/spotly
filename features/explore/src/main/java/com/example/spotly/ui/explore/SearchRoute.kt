package com.example.spotly.ui.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.SearchViewModel

@Composable
fun SearchRoute(viewModel: SearchViewModel, onUserClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(state, viewModel::changeQuery, viewModel::retry, onUserClick)
}
