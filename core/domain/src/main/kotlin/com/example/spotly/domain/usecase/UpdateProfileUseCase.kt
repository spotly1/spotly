package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.User
import com.example.spotly.domain.repository.ImageRepository
import com.example.spotly.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val profiles: ProfileRepository,
    private val images: ImageRepository
) {
    operator fun invoke(
        currentUser: User?, description: String, imageUri: String?, removeCurrentImage: Boolean,
        onResult: (AppResult<User?>) -> Unit
    ) {
        val trimmedDescription = description.trim()
        fun save(url: String, publicId: String) {
            profiles.updateProfile(trimmedDescription, url, publicId) { result ->
                when (result) {
                    is AppResult.Success -> onResult(AppResult.Success(
                        currentUser?.copy(description = trimmedDescription,
                            profileImageUrl = url, profileImagePublicId = publicId)
                    ))
                    is AppResult.Error -> onResult(result)
                }
            }
        }
        when {
            removeCurrentImage -> save("", "")
            imageUri == null -> save(currentUser?.profileImageUrl.orEmpty(), currentUser?.profileImagePublicId.orEmpty())
            else -> images.uploadImage(imageUri) { result ->
                when (result) {
                    is AppResult.Success -> save(result.data.url, result.data.publicId)
                    is AppResult.Error -> onResult(result)
                }
            }
        }
    }
}
