package com.example.spotly.data.repository

import com.example.spotly.domain.repository.UploadedImage
import com.example.spotly.network.ImageUploadDto

internal fun ImageUploadDto.toDomain(): UploadedImage? {
    val url = secureUrl ?: return null
    val id = publicId ?: return null
    return UploadedImage(url = url, publicId = id)
}
