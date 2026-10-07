package com.example.spotly.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.AuthViewModel

@Composable
fun LoginRoute(
    viewModel: AuthViewModel,
    onRegisterClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        state = state,
        onEdit = viewModel::clearError,
        onSubmit = { email, password ->
            viewModel.login(email, password)
        },
        onRegisterClick = { viewModel.clearError(); onRegisterClick() }
    )
}
