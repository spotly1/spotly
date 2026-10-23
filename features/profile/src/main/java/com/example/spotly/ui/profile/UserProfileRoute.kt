package com.example.spotly.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.spotly.viewmodel.UserProfileViewModel
import com.example.spotly.domain.model.FollowListType

@Composable
fun UserProfileRoute(
    uid: String,
    viewModel: UserProfileViewModel,
    onPostClick: (String) -> Unit,
    onFollowListClick: (String, FollowListType) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uid) {
        viewModel.loadProfile(uid)
    }

    UserProfileScreen(
        uiState = uiState,
        onRetry = { viewModel.loadProfile(uid) },
        onPostClick = onPostClick,
        onFollowClick = viewModel::toggleFollow,
        onFollowListClick = onFollowListClick
    )
}
