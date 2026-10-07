package com.example.spotly.data.repository

import com.example.spotly.network.UserDto
import org.junit.Assert.assertEquals
import org.junit.Test

class LayerMappersTest {
    @Test fun profileUpdateDoesNotOverwriteUsernameOrExposeEmail() {
        val updates = UserDto("agus", "bio", "photo", "id").toProfileUpdates()
        assertEquals(mapOf("description" to "bio", "profileImageUrl" to "photo",
            "profileImagePublicId" to "id"), updates)
    }
}
