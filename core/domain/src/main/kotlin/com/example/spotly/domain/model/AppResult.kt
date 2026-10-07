package com.example.spotly.domain.model

/** Resultado de una operación: no expone excepciones de proveedores a la UI. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.fold(onSuccess: (T) -> R, onError: (AppError) -> R): R =
    when (this) {
        is AppResult.Success -> onSuccess(data)
        is AppResult.Error -> onError(error)
    }
