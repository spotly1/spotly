package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import com.example.spotly.data.model.User
import com.example.spotly.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

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

    fun updateDescription(
        description: String,
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.updateDescription(
            description = description.trim(),
            onSuccess = {
                _user.value = _user.value?.copy(
                    description = description.trim()
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