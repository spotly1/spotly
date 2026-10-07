package com.example.spotly.data.repository

import com.example.spotly.domain.model.User
import com.example.spotly.network.PrivateUserDto
import com.example.spotly.network.UserDto

internal fun UserDto.toDomain(uid: String, privateUser: PrivateUserDto) = User(
    uid = uid,
    username = username,
    email = privateUser.email,
    description = description,
    profileImageUrl = profileImageUrl,
    profileImagePublicId = profileImagePublicId
)

// Actualizacion parcial: no modifica el username ni escribe datos privados.
internal fun UserDto.toProfileUpdates(): Map<String, String> = mapOf(
    "description" to description,
    "profileImageUrl" to profileImageUrl,
    "profileImagePublicId" to profileImagePublicId
)
