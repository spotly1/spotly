package com.example.spotly.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.ProfileViewModel
import com.example.spotly.viewmodel.ProfileUiState
import com.example.spotly.viewmodel.ProfileSaveState

@Composable
fun EditProfileRoute(viewModel: ProfileViewModel, onSaved: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnSaved by rememberUpdatedState(onSaved)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.uiState.collect { current ->
                if (current is ProfileUiState.Success && current.saveState is ProfileSaveState.Saved &&
                    viewModel.onSavedHandled()) {
                    currentOnSaved()
                }
            }
        }
    }

    when (val current = state) {
        ProfileUiState.Loading, is ProfileUiState.Error ->
            ProfileScreen(current, {}, {}, viewModel::loadCurrentUserProfile)
        is ProfileUiState.Success -> EditProfileScreen(
            user = current.user,
            isLoading = current.saveState is ProfileSaveState.Saving,
            errorMessage = (current.saveState as? ProfileSaveState.Error)?.error?.localizedMessage(),
            onSaveClick = { description, imageUri, removeCurrentImage ->
                viewModel.updateProfile(description, imageUri?.toString(), removeCurrentImage)
            }
        )
    }
}
