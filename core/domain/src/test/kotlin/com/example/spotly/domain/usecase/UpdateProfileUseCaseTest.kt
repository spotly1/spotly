package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.*
import org.junit.Assert.*
import org.junit.Test

class UpdateProfileUseCaseTest {
    private class FakeProfiles : ProfileRepository {
        var stored: List<String>? = null
        var reject = false
        override fun getCurrentUserProfile(onResult: (AppResult<User>) -> Unit) = onResult(AppResult.Success(User(uid = "alice")))
        override fun updateProfile(description: String, profileImageUrl: String, profileImagePublicId: String, onResult: (AppResult<Unit>) -> Unit) {
            stored = listOf(description, profileImageUrl, profileImagePublicId)
            if (reject) onResult(AppResult.Error(AppError.ProfileUpdateFailed)) else onResult(AppResult.Success(Unit))
        }
    }
    private class FakeImages : ImageRepository {
        var calls = 0
        var reject = false
        override fun uploadImage(imageUri: String, forPost: Boolean, onResult: (AppResult<UploadedImage>) -> Unit) {
            assertFalse(forPost)
            calls++
            if (reject) onResult(AppResult.Error(AppError.ImageUploadFailed)) else onResult(AppResult.Success(UploadedImage("new-url", "new-id")))
        }
    }
    private val user = User(uid = "alice", profileImageUrl = "old-url", profileImagePublicId = "old-id")

    @Test fun uploadsReplacementAndReturnsUpdatedUser() {
        val profiles = FakeProfiles()
        val images = FakeImages()
        var updated: User? = null
        UpdateProfileUseCase(profiles, images)(user, "Nueva", "content://selected", false) { result -> result.fold({ updated = it }, { fail(it.name) }) }
        assertEquals(listOf("Nueva", "new-url", "new-id"), profiles.stored)
        assertEquals("new-id", updated!!.profileImagePublicId)
        assertEquals(1, images.calls)
    }

    @Test fun uploadFailureDoesNotSaveProfile() {
        val profiles = FakeProfiles()
        val images = FakeImages().apply { reject = true }
        var error: AppError? = null
        UpdateProfileUseCase(profiles, images)(user, "", "content://selected", false) { result -> result.fold({ fail("No debe guardar") }, { error = it }) }
        assertEquals(AppError.ImageUploadFailed, error)
        assertNull(profiles.stored)
    }

    @Test fun saveFailureIsReturnedWithoutSuccess() {
        val profiles = FakeProfiles().apply { reject = true }
        var error: AppError? = null
        UpdateProfileUseCase(profiles, FakeImages())(user, "", null, false) { result -> result.fold({ fail("No debe confirmar") }, { error = it }) }
        assertEquals(AppError.ProfileUpdateFailed, error)
    }
}
