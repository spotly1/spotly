package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult

interface ImageRepository {
    fun uploadImage(
        imageUri: String, forPost: Boolean = false,
        onResult: (AppResult<UploadedImage>) -> Unit
    )
}
