package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.*
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.*
import org.junit.Test

class PublishPostUseCaseTest {
    private class FakeAuth(var uid: String? = "alice") : AuthRepository {
        override fun currentUserId() = uid
        override fun observeAuthentication() = emptyFlow<Boolean>()
        override fun logout() { uid = null }
        override fun login(email: String, password: String, onResult: (AppResult<Unit>) -> Unit) = error("Not used")
        override fun register(username: String, email: String, password: String, onResult: (AppResult<Unit>) -> Unit) = error("Not used")
    }
    private class FakePosts : PostRepository {
        var nextId = 0
        var failSave = false
        val savedIds = mutableListOf<String>()
        var description: String? = null
        var point: LocationPoint? = null
        override fun newId() = "id-${++nextId}"
        override fun observeFeed() = emptyFlow<AppResult<List<Post>>>()
        override fun observeUserPosts(uid: String) = emptyFlow<AppResult<List<Post>>>()
        override fun save(id: String, uid: String, image: UploadedImage, description: String, location: LocationPoint?, onResult: (AppResult<Unit>) -> Unit) {
            savedIds.add(id)
            this.description = description
            point = location
            if (failSave) onResult(AppResult.Error(AppError.PostSaveFailed)) else onResult(AppResult.Success(Unit))
        }
    }
    private class FakeImages : ImageRepository {
        var calls = 0
        var beforeSuccess: () -> Unit = {}
        override fun uploadImage(imageUri: String, forPost: Boolean, onResult: (AppResult<UploadedImage>) -> Unit) {
            assertTrue(forPost)
            calls++
            beforeSuccess()
            onResult(AppResult.Success(UploadedImage("url", "public-id")))
        }
    }

    @Test fun rejectsInvalidDraftsBeforeCreatingIdOrUploading() {
        val posts = FakePosts()
        val images = FakeImages()
        val publish = PublishPostUseCase(posts, images, FakeAuth())
        for ((uri, description) in listOf(null to "Foto", "photo" to " ", "photo" to "a".repeat(1001))) {
            var error: AppError? = null
            publish(uri, description, null) { result -> result.fold({ fail("No debe publicar") }, { error = it }) }
            assertEquals(AppError.RequiredFields, error)
        }
        assertEquals(0, images.calls)
        assertEquals(0, posts.nextId)
    }

    @Test fun logoutDuringUploadPreventsWritingPost() {
        val auth = FakeAuth()
        val posts = FakePosts()
        val images = FakeImages().apply { beforeSuccess = { auth.logout() } }
        var error: AppError? = null
        PublishPostUseCase(posts, images, auth)("photo", "Plaza", null) { result -> result.fold({ fail("No debe publicar") }, { error = it }) }
        assertEquals(AppError.Unauthenticated, error)
        assertTrue(posts.savedIds.isEmpty())
    }

}
