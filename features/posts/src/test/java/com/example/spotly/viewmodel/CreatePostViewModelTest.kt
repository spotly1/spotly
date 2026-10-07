package com.example.spotly.viewmodel

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.*
import com.example.spotly.domain.usecase.*
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.*
import org.junit.Test

class CreatePostViewModelTest {
    private class FakeAuth(var uid: String? = "alice") : AuthRepository {
        override fun currentUserId() = uid
        override fun observeAuthentication() = emptyFlow<Boolean>()
        override fun logout() { uid = null }
        override fun login(email: String, password: String, onResult: (AppResult<Unit>) -> Unit) = error("Not used")
        override fun register(username: String, email: String, password: String, onResult: (AppResult<Unit>) -> Unit) = error("Not used")
    }
    private class FakePosts : PostRepository {
        var description: String? = null
        var failSave = false
        var ids = mutableListOf<String>()
        override fun newId() = "stable-id"
        override fun observeFeed() = emptyFlow<AppResult<List<Post>>>()
        override fun observeUserPosts(uid: String) = emptyFlow<AppResult<List<Post>>>()
        override fun save(id: String, uid: String, image: UploadedImage, description: String, location: LocationPoint?, onResult: (AppResult<Unit>) -> Unit) {
            this.description = description
            ids.add(id)
            if (failSave) onResult(AppResult.Error(AppError.PostSaveFailed)) else onResult(AppResult.Success(Unit))
        }
    }
    private class FakeImages : ImageRepository {
        var uploads = 0
        override fun uploadImage(imageUri: String, forPost: Boolean, onResult: (AppResult<UploadedImage>) -> Unit) {
            uploads++
            onResult(AppResult.Success(UploadedImage("https://example.com/image", "public-id")))
        }
    }
    private val locations = object : LocationRepository {
        override suspend fun currentLocation(): AppResult<LocationPoint> = AppResult.Error(AppError.LocationUnavailable)
    }

    @Test fun publishesThroughContractsAndPreservesRetryImage() {
        val posts = FakePosts().apply { failSave = true }
        val images = FakeImages()
        val vm = CreatePostViewModel(PublishPostUseCase(posts, images, FakeAuth()), GetCurrentLocationUseCase(locations))
        vm.publish("content://photo", " Una plaza ")
        assertEquals(AppError.PostSaveFailed, vm.uiState.value.error)
        posts.failSave = false
        vm.publish("content://photo", " Una plaza ")
        assertEquals("Una plaza", posts.description)
        assertEquals(1, images.uploads)
        assertEquals(listOf("stable-id", "stable-id"), posts.ids)
        assertTrue(vm.uiState.value.published)
        // El resultado queda pendiente hasta consumirlo y no permite publicar dos veces.
        vm.publish("content://photo", " Una plaza ")
        assertEquals(2, posts.ids.size)
        assertTrue(vm.onPublishedHandled())
        assertTrue(vm.uiState.value is CreatePostUiState.Editing)
        assertFalse(vm.onPublishedHandled())
        vm.publish("content://photo", " Otra plaza ")
        assertEquals(2, images.uploads)
    }

    @Test fun unauthenticatedUserDoesNotUpload() {
        val images = FakeImages()
        val vm = CreatePostViewModel(PublishPostUseCase(FakePosts(), images, FakeAuth(null)), GetCurrentLocationUseCase(locations))
        vm.publish("content://photo", "Una plaza")
        assertEquals(AppError.Unauthenticated, vm.uiState.value.error)
        assertEquals(0, images.uploads)
    }
}
