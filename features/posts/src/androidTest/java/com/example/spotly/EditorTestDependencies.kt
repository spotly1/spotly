package com.example.spotly

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.*
import com.example.spotly.domain.usecase.*
import com.example.spotly.viewmodel.CreatePostViewModel
import kotlinx.coroutines.flow.emptyFlow

/** El test de edición/restauración no debe acceder a red, sesión ni ubicación reales. */
internal fun createEditorTestViewModel() = editorViewModel(
    repository = object : PostRepository {
        override fun newId(): String = error("No se publica durante esta prueba")
        override fun observeFeed() = emptyFlow<AppResult<List<Post>>>()
        override fun observeUserPosts(uid: String) = emptyFlow<AppResult<List<Post>>>()
        override fun save(id: String, uid: String, image: UploadedImage, description: String, location: LocationPoint?, onResult: (AppResult<Unit>) -> Unit) {
            error("No se publica durante esta prueba")
        }
    },
    images = object : ImageRepository {
        override fun uploadImage(imageUri: String, forPost: Boolean, onResult: (AppResult<UploadedImage>) -> Unit) {
            error("No se suben imágenes durante esta prueba")
        }
    },
    locations = object : LocationRepository {
        override suspend fun currentLocation(): AppResult<LocationPoint> = error("No se solicita ubicación durante esta prueba")
    },
    auth = object : AuthRepository {
        override fun currentUserId(): String? = null
        override fun observeAuthentication() = emptyFlow<Boolean>()
        override fun login(email: String, password: String, onResult: (AppResult<Unit>) -> Unit) {
            error("No se inicia sesión durante esta prueba")
        }
        override fun register(username: String, email: String, password: String, onResult: (AppResult<Unit>) -> Unit) {
            error("No se crean cuentas durante esta prueba")
        }
        override fun logout() = Unit
    }
)

private fun editorViewModel(repository: PostRepository, images: ImageRepository, locations: LocationRepository, auth: AuthRepository) =
    CreatePostViewModel(PublishPostUseCase(repository, images, auth), GetCurrentLocationUseCase(locations))
