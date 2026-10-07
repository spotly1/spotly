package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.usecase.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val getCurrentProfile: GetCurrentProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val observeUserPosts: ObserveUserPostsUseCase
) : ViewModel() {
    private val profile = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)

    // Cambiar el estado de guardado no reinicia la escucha de publicaciones.
    private data class UserPosts(val uid: String?, val state: ProfilePostsState)
    private val posts = profile.map { it.user?.uid }.distinctUntilChanged().flatMapLatest { uid ->
        if (uid == null) flowOf(UserPosts(null, ProfilePostsState.Loading))
        else observeUserPosts(uid).map { result ->
            UserPosts(uid, when (result) {
                is AppResult.Success -> ProfilePostsState.Success(result.data)
                is AppResult.Error -> ProfilePostsState.Error(result.error)
            })
        }.onStart { emit(UserPosts(uid, ProfilePostsState.Loading)) }
    }

    val uiState: StateFlow<ProfileUiState> = combine(profile, posts) { current, userPosts ->
        when (current) {
            is ProfileUiState.Success -> current.copy(
                postsState = if (userPosts.uid == current.user.uid) userPosts.state else ProfilePostsState.Loading
            )
            ProfileUiState.Loading, is ProfileUiState.Error -> current
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 0), ProfileUiState.Loading)

    fun loadCurrentUserProfile() {
        profile.value = ProfileUiState.Loading
        getCurrentProfile { result ->
            profile.value = when (result) {
                is AppResult.Success -> ProfileUiState.Success(result.data)
                is AppResult.Error -> ProfileUiState.Error(result.error)
            }
        }
    }

    fun updateProfile(description: String, imageUri: String?, removeCurrentImage: Boolean) {
        val current = profile.value as? ProfileUiState.Success ?: return
        if (current.saveState is ProfileSaveState.Saving || current.saveState is ProfileSaveState.Saved) return
        profile.value = current.copy(saveState = ProfileSaveState.Saving)
        updateProfileUseCase(current.user, description, imageUri, removeCurrentImage) { result ->
            profile.value = when (result) {
                is AppResult.Success -> {
                    val user = result.data
                    if (user != null) current.copy(user = user, saveState = ProfileSaveState.Saved)
                    else current.copy(saveState = ProfileSaveState.Error(AppError.ProfileNotFound))
                }
                is AppResult.Error -> current.copy(saveState = ProfileSaveState.Error(result.error))
            }
        }
    }

    /** Consume una sola vez, incluso si el Flow todavía conserva una emisión anterior. */
    fun onSavedHandled(): Boolean {
        val current = profile.value as? ProfileUiState.Success ?: return false
        if (current.saveState !is ProfileSaveState.Saved) return false
        profile.value = current.copy(saveState = ProfileSaveState.Idle)
        return true
    }
}
