package com.example.spotly.domain.model

data class FollowSummary(
    val followers: Int,
    val following: Int,
    val isFollowing: Boolean
)
