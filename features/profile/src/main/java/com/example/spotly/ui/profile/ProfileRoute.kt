package com.example.spotly.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.ProfileViewModel

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadCurrentUserProfile() }
    ProfileScreen(uiState, onEditProfileClick, onLogoutClick, viewModel::loadCurrentUserProfile)
}
