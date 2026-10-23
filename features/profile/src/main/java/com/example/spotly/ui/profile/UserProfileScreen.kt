package com.example.spotly.ui.profile

import androidx.compose.runtime.Composable
import com.example.spotly.domain.model.AppError
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.UserProfileUiState
import com.example.spotly.domain.model.FollowListType

@Composable
fun UserProfileScreen(
    uiState: UserProfileUiState,
    onRetry: () -> Unit,
    onPostClick: (String) -> Unit,
    onFollowClick: () -> Unit,
    onFollowListClick: (String, FollowListType) -> Unit
) {
    when (uiState) {
        UserProfileUiState.Loading -> {
            LoadingProfile()
        }

        is UserProfileUiState.Error -> {
            ProfileError(
                message = uiState.error.localizedMessage(),
                onRetry = onRetry
            )
        }

        is UserProfileUiState.Success -> {
            ProfileContent(
                user = uiState.user,
                postsState = uiState.postsState,
                isOwnProfile = false,
                followState = uiState.followState,
                followBusy = uiState.followBusy,
                followError = uiState.followError,
                onFollowClick = onFollowClick,
                onRetryPosts = onRetry,
                onPostClick = onPostClick,
                onFollowListClick = onFollowListClick
            )
        }
    }
}
