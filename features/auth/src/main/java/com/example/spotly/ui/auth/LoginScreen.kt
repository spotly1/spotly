package com.example.spotly.ui.auth

import androidx.compose.runtime.Composable
import com.example.spotly.viewmodel.AuthUiState

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEdit: () -> Unit,
    onSubmit: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    AuthForm(
        register = false,
        state = state,
        onEdit = onEdit,
        onSubmit = { _, email, password, _ -> onSubmit(email, password) },
        onSwitch = onRegisterClick
    )
}
