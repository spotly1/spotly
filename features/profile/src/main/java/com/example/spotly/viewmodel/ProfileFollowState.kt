package com.example.spotly.viewmodel

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.FollowSummary
import com.example.spotly.domain.usecase.ObserveFollowProfileUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class ProfileFollowState(
    val uid: String? = null,
    val summary: FollowSummary? = null,
    val error: AppError? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
internal fun Flow<String?>.followState(observe: ObserveFollowProfileUseCase): Flow<ProfileFollowState> =
    distinctUntilChanged().flatMapLatest { uid ->
        if (uid == null) flowOf(ProfileFollowState())
        else observe(uid).map { result ->
            when (result) {
                is AppResult.Success -> ProfileFollowState(uid, summary = result.data)
                is AppResult.Error -> ProfileFollowState(uid, error = result.error)
            }
        }.onStart { emit(ProfileFollowState(uid)) }
    }
