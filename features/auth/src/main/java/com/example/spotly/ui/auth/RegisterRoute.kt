package com.example.spotly.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.AuthViewModel

@Composable
fun RegisterRoute(
    viewModel: AuthViewModel,
    onLoginClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterScreen(
        state = state,
        onEdit = viewModel::clearError,
        onSubmit = { username, email, password, confirmation ->
            viewModel.register(username, email, password, confirmation)
        },
        onLoginClick = { viewModel.clearError(); onLoginClick() }
    )
}
