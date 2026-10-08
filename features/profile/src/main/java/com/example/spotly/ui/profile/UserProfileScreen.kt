package com.example.spotly.ui.profile

import androidx.compose.runtime.Composable
import com.example.spotly.domain.model.AppError
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.UserProfileUiState

@Composable
fun UserProfileScreen(
    uiState: UserProfileUiState,
    onRetry: () -> Unit
) {
    when (uiState) {
        UserProfileUiState.Loading -> {
            LoadingProfile()
        }

        is UserProfileUiState.Error -> {
            ProfileError(uiState.error.localizedMessage())
        }

        is UserProfileUiState.Success -> {
            ProfileContent(
                user = uiState.user,
                postsState = uiState.postsState,
                isOwnProfile = false,
                onRetryPosts = onRetry
            )
        }
    }
}