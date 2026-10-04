package com.example.spotly.ui.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.spotly.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onLogoutClick: () -> Unit
) {
    val username by viewModel.username.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrentUsername()
    }

    Column {
        Text(
            text = if (username != null) {
                "Perfil: @$username"
            } else {
                "Cargando perfil..."
            }
        )

        Button(
            onClick = onLogoutClick
        ) {
            Text("Cerrar sesión")
        }
    }
}