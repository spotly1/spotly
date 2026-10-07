package com.example.spotly

import com.example.spotly.core.ui.R

import android.graphics.Bitmap
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.Post
import com.example.spotly.ui.feed.FeedScreen
import com.example.spotly.ui.theme.SpotlyTheme
import com.example.spotly.viewmodel.FeedUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FeedScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun emptyErrorRetryAndPost() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var state by mutableStateOf<FeedUiState>(FeedUiState.Success())
        var retries = 0
        compose.setContent { SpotlyTheme { Surface { FeedScreen(state) { retries++ } } } }
        compose.onNodeWithText(context.getString(R.string.no_posts_yet)).assertIsDisplayed()
        compose.runOnIdle { state = FeedUiState.Error(AppError.FeedLoadFailed) }
        compose.onNodeWithText(context.getString(R.string.retry)).performClick()
        compose.runOnIdle {
            assertEquals(1, retries)
            val photo = File(context.cacheDir, "feed-test.png")
            val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
            photo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            state = FeedUiState.Success(posts = listOf(Post(
                id = "local-test", username = "spotly", description = "Un mural en mi barrio",
                imageUrl = android.net.Uri.fromFile(photo).toString()
            )))
        }
        compose.onNodeWithText("@spotly").assertIsDisplayed()
        compose.onNodeWithText("Un mural en mi barrio").assertIsDisplayed()
        compose.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(context.getExternalFilesDir(null), "feed-review.png").outputStream().use {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        screenshot.recycle()
    }
}
