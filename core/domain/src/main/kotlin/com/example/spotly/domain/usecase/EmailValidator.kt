package com.example.spotly.domain.usecase

/** El adaptador de app conserva la validación de email existente sin importar Android aquí. */
fun interface EmailValidator {
    fun isValid(email: String): Boolean
}
