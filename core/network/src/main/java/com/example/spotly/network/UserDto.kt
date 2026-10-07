package com.example.spotly.network

// Document users/{uid}: nunca contiene el correo privado.
data class UserDto(
    val username: String = "",
    val description: String = "",
    val profileImageUrl: String = "",
    val profileImagePublicId: String = ""
)

// Document privateUsers/{uid}: separado del perfil publico.
data class PrivateUserDto(val email: String = "")

data class UsernameReservationDto(val uid: String = "")
