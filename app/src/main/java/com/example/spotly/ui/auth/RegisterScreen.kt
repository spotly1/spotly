package com.example.spotly.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.clearError() }
    AuthForm(
        register = true,
        state = state,
        onEdit = viewModel::clearError,
        onSubmit = { username, email, password, confirmation ->
            viewModel.register(username, email, password, confirmation, onRegisterSuccess)
        },
        onSwitch = onLoginClick
    )
}
