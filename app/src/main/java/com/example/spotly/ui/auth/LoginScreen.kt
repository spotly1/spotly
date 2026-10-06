package com.example.spotly.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.clearError() }
    AuthForm(
        register = false,
        state = state,
        onEdit = viewModel::clearError,
        onSubmit = { _, email, password, _ ->
            viewModel.login(email, password, onLoginSuccess)
        },
        onSwitch = onRegisterClick
    )
}
