package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.usecase.GetUserProfileUseCase
import com.example.spotly.domain.usecase.ObserveUserPostsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModel(
    private val getUserProfile: GetUserProfileUseCase,
    private val observeUserPosts: ObserveUserPostsUseCase
) : ViewModel() {

    private val profile =
        MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)

    private data class UserPosts(
        val uid: String?,
        val state: ProfilePostsState
    )

    private val posts = profile
        .map { (it as? UserProfileUiState.Success)?.user?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid == null) {
                flowOf(UserPosts(null, ProfilePostsState.Loading))
            } else {
                observeUserPosts(uid)
                    .map { result ->
                        UserPosts(
                            uid = uid,
                            state = when (result) {
                                is AppResult.Success ->
                                    ProfilePostsState.Success(result.data)

                                is AppResult.Error ->
                                    ProfilePostsState.Error(result.error)
                            }
                        )
                    }
                    .onStart {
                        emit(UserPosts(uid, ProfilePostsState.Loading))
                    }
            }
        }

    val uiState: StateFlow<UserProfileUiState> =
        combine(profile, posts) { current, userPosts ->
            when (current) {
                is UserProfileUiState.Success -> current.copy(
                    postsState = if (userPosts.uid == current.user.uid) {
                        userPosts.state
                    } else {
                        ProfilePostsState.Loading
                    }
                )

                else -> current
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserProfileUiState.Loading
        )

    private var requestVersion = 0

    fun loadProfile(uid: String) {
        val version = ++requestVersion
        profile.value = UserProfileUiState.Loading

        getUserProfile(uid) { result ->
            if (version != requestVersion) return@getUserProfile

            profile.value = when (result) {
                is AppResult.Success ->
                    UserProfileUiState.Success(result.data)

                is AppResult.Error ->
                    UserProfileUiState.Error(result.error)
            }
        }
    }
}