package com.example.spotly.viewmodel

import com.example.spotly.data.model.AppError

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.net.Uri
import com.example.spotly.data.model.User
import com.example.spotly.data.repository.ImageRepository
import com.example.spotly.data.repository.ProfileRepository
import com.example.spotly.data.repository.PostRepository
import com.example.spotly.data.model.Post
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val posts: List<Post> = emptyList(),
    val postsLoading: Boolean = true,
    val postsError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProfileRepository()
    private val imageRepository = ImageRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    private val postRepository = PostRepository()
    val uiState: StateFlow<ProfileUiState> = _uiState.flatMapLatest { profile ->
        val uid = profile.user?.uid
        if (uid == null) flowOf(profile)
        else postRepository.observeUserPosts(uid).map { result ->
            result.fold(
                onSuccess = { profile.copy(posts = it, postsLoading = false) },
                onFailure = { profile.copy(postsLoading = false, postsError = true) }
            )
        }.onStart { emit(profile.copy(postsLoading = true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 0), ProfileUiState())

    fun loadCurrentUserProfile() {
        _uiState.value = ProfileUiState(isLoading = true)

        repository.getCurrentUserProfile(
            onSuccess = { user ->
                _uiState.value = ProfileUiState(user = user)
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = error)
            }
        )
    }

    fun updateProfile(
        description: String,
        imageUri: Uri?,
        removeCurrentImage: Boolean,
        onSuccess: () -> Unit
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        val trimmedDescription = description.trim()

        if (removeCurrentImage) {
            saveProfile(
                description = trimmedDescription,
                profileImageUrl = "",
                profileImagePublicId = "",
                onSuccess = onSuccess
            )
            return
        }

        if (imageUri == null) {
            saveProfile(
                description = trimmedDescription,
                profileImageUrl = _uiState.value.user?.profileImageUrl.orEmpty(),
                profileImagePublicId = _uiState.value.user?.profileImagePublicId.orEmpty(),
                onSuccess = onSuccess
            )
            return
        }

        imageRepository.uploadImage(
            imageUri = imageUri,
            onSuccess = { image ->
                saveProfile(
                    description = trimmedDescription,
                    profileImageUrl = image.url,
                    profileImagePublicId = image.publicId,
                    onSuccess = onSuccess
                )
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = error)
            }
        )
    }

    private fun saveProfile(
        description: String,
        profileImageUrl: String,
        profileImagePublicId: String,
        onSuccess: () -> Unit
    ) {
        repository.updateProfile(
            description = description,
            profileImageUrl = profileImageUrl,
            profileImagePublicId = profileImagePublicId,
            onSuccess = {
                val updatedUser = _uiState.value.user?.copy(
                    description = description,
                    profileImageUrl = profileImageUrl,
                    profileImagePublicId = profileImagePublicId
                )
                _uiState.value = ProfileUiState(user = updatedUser)
                onSuccess()
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = error)
            }
        )
    }
}
