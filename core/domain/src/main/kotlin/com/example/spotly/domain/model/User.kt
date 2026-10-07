package com.example.spotly.domain.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val description: String = "",
    val profileImageUrl: String = "",
    val profileImagePublicId: String = ""
)
