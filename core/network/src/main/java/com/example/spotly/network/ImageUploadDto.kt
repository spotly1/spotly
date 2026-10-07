package com.example.spotly.network

data class ImageUploadDto(val secureUrl: String?, val publicId: String?) {
    companion object {
        fun fromResponse(response: Map<*, *>) = ImageUploadDto(
            secureUrl = response["secure_url"] as? String,
            publicId = response["public_id"] as? String
        )
    }
}
