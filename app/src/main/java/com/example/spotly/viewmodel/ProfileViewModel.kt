package com.example.spotly.viewmodel

import com.example.spotly.data.model.User
import com.example.spotly.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.spotly.data.repository.ImageRepository
import android.net.Uri

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProfileRepository()
    private val imageRepository = ImageRepository(application)

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadCurrentUserProfile() {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getCurrentUserProfile(
            onSuccess = { user ->
                _user.value = user
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    fun updateProfile(
        description: String,
        imageUri: Uri?,
        removeCurrentImage: Boolean,
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        val trimmedDescription = description.trim()

        if (removeCurrentImage) {
            saveProfile(
                description = trimmedDescription,
                profileImageUrl = "",
                onSuccess = onSuccess
            )
            return
        }

        if (imageUri == null) {
            saveProfile(
                description = trimmedDescription,
                profileImageUrl = _user.value?.profileImageUrl.orEmpty(),
                onSuccess = onSuccess
            )
            return
        }

        imageRepository.uploadImage(
            imageUri = imageUri,
            onSuccess = { imageUrl ->
                saveProfile(
                    description = trimmedDescription,
                    profileImageUrl = imageUrl,
                    onSuccess = onSuccess
                )
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    private fun saveProfile(
        description: String,
        profileImageUrl: String,
        onSuccess: () -> Unit
    ) {
        repository.updateProfile(
            description = description,
            profileImageUrl = profileImageUrl,
            onSuccess = {
                _user.value = _user.value?.copy(
                    description = description,
                    profileImageUrl = profileImageUrl
                )

                _isLoading.value = false
                onSuccess()
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }
}