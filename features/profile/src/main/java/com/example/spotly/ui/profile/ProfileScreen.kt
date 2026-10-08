package com.example.spotly.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.example.spotly.viewmodel.ProfileUiState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.core.ui.R
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRetryPosts: () -> Unit
) {
    when (uiState) {
        ProfileUiState.Loading -> LoadingProfile()

        is ProfileUiState.Error ->
            ProfileError(uiState.error.localizedMessage())

        is ProfileUiState.Success -> {
            ProfileContent(
                user = uiState.user,
                postsState = uiState.postsState,
                isOwnProfile = true,
                onEditProfileClick = onEditProfileClick,
                onLogoutClick = onLogoutClick,
                onRetryPosts = onRetryPosts
            )
        }
    }
}

@Composable
internal fun LoadingProfile() {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun ProfileError(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Text(message.ifBlank { stringResource(R.string.profile_load_error) })

        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}
