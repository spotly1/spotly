package com.example.spotly.ui.auth

import androidx.compose.runtime.Composable
import com.example.spotly.viewmodel.AuthUiState

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onEdit: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    AuthForm(
        register = true,
        state = state,
        onEdit = onEdit,
        onSubmit = onSubmit,
        onSwitch = onLoginClick
    )
}
