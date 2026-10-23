package com.example.spotly.domain.model

data class FeedPage(val posts: List<Post>, val hasFollowing: Boolean,
    val isCached: Boolean = false, val refreshError: AppError? = null)
