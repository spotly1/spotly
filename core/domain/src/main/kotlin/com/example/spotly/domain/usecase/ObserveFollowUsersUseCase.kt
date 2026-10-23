package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.FollowListType
import com.example.spotly.domain.repository.FollowRepository

class ObserveFollowUsersUseCase(private val repository: FollowRepository) {
    operator fun invoke(uid: String, type: FollowListType) = repository.observeUsers(uid, type)
}
