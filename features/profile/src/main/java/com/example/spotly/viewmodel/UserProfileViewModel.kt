package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.usecase.GetUserProfileUseCase
import com.example.spotly.domain.usecase.ObserveFollowProfileUseCase
import com.example.spotly.domain.usecase.SetFollowingUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
    private val observeUserPosts: ObserveUserPostsUseCase,
    observeFollows: ObserveFollowProfileUseCase,
    private val setFollowing: SetFollowingUseCase
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

    private val follows = profile.map { (it as? UserProfileUiState.Success)?.user?.uid }.followState(observeFollows)
    private var followJob: Job? = null

    val uiState: StateFlow<UserProfileUiState> =
        combine(profile, posts, follows) { current, userPosts, following ->
            when (current) {
                is UserProfileUiState.Success -> current.copy(
                    postsState = if (userPosts.uid == current.user.uid) {
                        userPosts.state
                    } else {
                        ProfilePostsState.Loading
                    },
                    followState = if (following.uid == current.user.uid) following else ProfileFollowState()
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
        followJob?.cancel()
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

    fun toggleFollow() {
        val visible = uiState.value as? UserProfileUiState.Success ?: return
        val summary = visible.followState.summary ?: return
        val current = profile.value as? UserProfileUiState.Success ?: return
        if (current.followBusy || current.user.uid != visible.user.uid) return
        val version = requestVersion
        profile.value = current.copy(followBusy = true, followError = null)
        followJob = viewModelScope.launch {
            val result = setFollowing(current.user.uid, !summary.isFollowing)
            if (version != requestVersion) return@launch
            val latest = profile.value as? UserProfileUiState.Success ?: return@launch
            profile.value = latest.copy(followBusy = false, followError = (result as? AppResult.Error)?.error)
        }
    }
}
