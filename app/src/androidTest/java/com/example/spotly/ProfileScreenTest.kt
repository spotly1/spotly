package com.example.spotly

import android.graphics.Bitmap
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.spotly.data.model.Post
import com.example.spotly.data.model.User
import com.example.spotly.ui.profile.ProfileContent
import com.example.spotly.ui.theme.SpotlyTheme
import com.example.spotly.viewmodel.ProfileUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun countPhotosUpdatesAndError() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val photo = File(context.cacheDir, "profile-post-test.png")
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
        photo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val posts = (1..2).map { Post(id = "$it", authorId = "alice", description = "Foto $it",
            imageUrl = android.net.Uri.fromFile(photo).toString()) }
        var state by mutableStateOf(ProfileUiState(user = User(uid = "alice", username = "alice"), postsLoading = false))
        var retries = 0
        compose.setContent { SpotlyTheme { Surface { ProfileContent(state, {}, {}, { retries++ }) } } }
        compose.onNodeWithText(context.getString(R.string.no_posts_yet)).performScrollTo().assertIsDisplayed()
        compose.runOnIdle { state = state.copy(posts = posts) }
        compose.onNodeWithText("2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.no_posts_yet)).assertDoesNotExist()
        compose.onNodeWithContentDescription("Foto 1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("@alice").performScrollTo()
        compose.waitUntil(timeoutMillis = 5000) {
            val pixels = compose.onNodeWithContentDescription("Foto 1").captureToImage().toPixelMap()
            val color = pixels[pixels.width / 4, pixels.height / 4]
            color.red > 0.8f && color.green < 0.6f && color.blue < 0.4f
        }
        val screenshot = compose.onRoot().captureToImage().asAndroidBitmap()
        File(context.getExternalFilesDir(null), "profile-review.png").outputStream().use {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithContentDescription("Foto 2").performScrollTo().performClick()
        compose.onNodeWithText("Foto 2").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.back_to_profile)).performClick()
        compose.onNodeWithText("2").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { state = state.copy(posts = posts + Post(id = "3", description = "Foto 3")) }
        compose.onNodeWithText("3").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { state = state.copy(posts = emptyList(), postsError = true) }
        compose.onNodeWithText(context.getString(R.string.count_unavailable)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.no_posts_yet)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.retry)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }

    @Test fun selectedPhotoOpensAndScrollsToNextPost() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val photo = File(context.cacheDir, "grid-test.png")
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
        photo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val posts = (1..6).map { Post(id = "$it", username = "alice", description = "Foto $it",
            imageUrl = android.net.Uri.fromFile(photo).toString()) }
        val state = ProfileUiState(user = User(uid = "alice", username = "alice"), posts = posts, postsLoading = false)
        compose.setContent { SpotlyTheme { Surface { ProfileContent(state, {}, {}, {}) } } }
        compose.onNodeWithContentDescription("Foto 2").performScrollTo().performClick()
        compose.onNodeWithText("Foto 2").assertIsDisplayed()
        compose.onNodeWithText("Foto 3").performScrollTo().assertIsDisplayed()
        val screenshot = compose.onRoot().captureToImage().asAndroidBitmap()
        File(context.getExternalFilesDir(null), "profile-posts-review.png").outputStream().use {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithContentDescription(context.getString(R.string.back_to_profile)).performClick()
        compose.onNodeWithText("6").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Foto 3").performScrollTo().performClick()
        compose.onNodeWithText("Foto 3").assertIsDisplayed()
    }
}
