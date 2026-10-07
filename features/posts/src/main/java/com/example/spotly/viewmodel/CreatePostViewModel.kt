package com.example.spotly.viewmodel

import com.example.spotly.domain.usecase.*
import androidx.lifecycle.ViewModel
import com.example.spotly.domain.model.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val publishPost: PublishPostUseCase,
    private val getCurrentLocation: GetCurrentLocationUseCase
) : ViewModel() {
    private val state = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Editing())
    val uiState = state.asStateFlow()

    fun locate() {
        val draft = state.value as? CreatePostUiState.Editing ?: return
        state.value = CreatePostUiState.Locating(draft.location)
        viewModelScope.launch {
            state.value = when (val result = getCurrentLocation()) {
                is AppResult.Success -> draft.copy(location = result.data, locationError = false)
                is AppResult.Error -> draft.copy(locationError = true)
            }
        }
    }

    fun locationDenied() {
        val draft = state.value as? CreatePostUiState.Editing ?: return
        state.value = draft.copy(locationError = true)
    }

    fun removeLocation() {
        val draft = state.value as? CreatePostUiState.Editing ?: return
        state.value = draft.copy(location = null, locationError = false)
    }

    fun publish(uri: String?, description: String) {
        val draft = state.value as? CreatePostUiState.Editing ?: return
        state.value = CreatePostUiState.Publishing(draft.location)
        publishPost(uri, description, draft.location) { result ->
            state.value = when (result) {
                is AppResult.Success -> CreatePostUiState.Published
                is AppResult.Error -> draft.copy(error = result.error)
            }
        }
    }

    /** Consume la publicación una sola vez antes de navegar, sin puntos de suspensión. */
    fun onPublishedHandled(): Boolean {
        if (state.value !is CreatePostUiState.Published) return false
        state.value = CreatePostUiState.Editing()
        publishPost.resetDraft()
        return true
    }
}
