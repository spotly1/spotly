package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.LocationPoint
import com.example.spotly.domain.repository.*

/** Una instancia por editor/ViewModel: conserva la imagen y el ID entre reintentos del borrador. */
class PublishPostUseCase(
    private val posts: PostRepository,
    private val images: ImageRepository,
    private val auth: AuthRepository
) {
    private var pendingKey: Pair<String, String>? = null
    private var pendingImage: UploadedImage? = null
    private var pendingId: String? = null

    operator fun invoke(
        uri: String?, description: String, location: LocationPoint?,
        onResult: (AppResult<Unit>) -> Unit
    ) {
        if (uri == null || description.isBlank() || description.length > 1000) {
            onResult(AppResult.Error(AppError.RequiredFields))
            return
        }
        val uid = auth.currentUserId()
        if (uid == null) { onResult(AppResult.Error(AppError.Unauthenticated)); return }
        val key = uid to uri
        if (pendingKey != key) {
            pendingKey = key
            pendingImage = null
            pendingId = posts.newId()
        }
        fun save(image: UploadedImage) {
            if (auth.currentUserId() != uid) { onResult(AppResult.Error(AppError.Unauthenticated)); return }
            posts.save(requireNotNull(pendingId), uid, image, description.trim(), location, onResult)
        }
        val image = pendingImage
        if (image != null) save(image)
        else images.uploadImage(uri, forPost = true) { result ->
            when (result) {
                is AppResult.Success -> {
                    pendingImage = result.data
                    save(result.data)
                }
                is AppResult.Error -> onResult(result)
            }
        }
    }

    fun resetDraft() {
        pendingKey = null
        pendingImage = null
        pendingId = null
    }
}
