package com.example.spotly

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.spotly.ui.post.CreatePostContent
import com.example.spotly.ui.post.CreatePostScreen
import com.example.spotly.ui.theme.SpotlyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

@org.junit.runner.RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class CreatePostScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun preparePhotoDescriptionAndRemovePhoto() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val photo = File(context.cacheDir, "post-test.png")
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
        photo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        var selectedImage by mutableStateOf<String?>(null)
        var description by mutableStateOf("")
        var galleryRequests = 0
        var publishRequests = 0
        var cameraRequests = 0
        compose.setContent {
            SpotlyTheme {
                androidx.compose.material3.Surface {
                    CreatePostContent(
                        imageUri = selectedImage,
                        description = description,
                        onChooseImage = { galleryRequests++ },
                        onTakePhoto = { cameraRequests++ },
                        onRemoveImage = { selectedImage = null },
                        onDescriptionChange = { description = it },
                        onPublish = { publishRequests++ }
                    )
                }
            }
        }
        compose.onNodeWithText(context.getString(R.string.post_empty_photo)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.post_publish)).performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText(context.getString(R.string.take_photo)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, cameraRequests) }
        compose.onNodeWithText(context.getString(R.string.choose_from_gallery)).performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(1, galleryRequests)
            selectedImage = android.net.Uri.fromFile(photo).toString()
        }
        compose.onNodeWithContentDescription(context.getString(R.string.post_photo_preview)).assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performScrollTo().performTextInput("Un mural en mi barrio")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.runOnIdle { assertEquals("Un mural en mi barrio", description) }
        compose.onNodeWithText(context.getString(R.string.post_publish)).performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(1, publishRequests) }
        compose.onNodeWithText(context.getString(R.string.create_post)).performScrollTo()
        compose.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(context.getExternalFilesDir(null), "create-post-review.png").outputStream().use {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        screenshot.recycle()
        compose.onNodeWithText(context.getString(R.string.delete_photo)).performScrollTo().performClick()
        compose.onNodeWithText(context.getString(R.string.post_empty_photo)).performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals("Un mural en mi barrio", description) }
        compose.onNodeWithText(context.getString(R.string.post_publish)).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun descriptionSurvivesRecreationAndRejectsOverLimit() {
        val restoration = androidx.compose.ui.test.junit4.StateRestorationTester(compose)
        restoration.setContent { SpotlyTheme { CreatePostScreen() } }
        compose.onNode(hasSetTextAction()).performScrollTo().performTextInput("Mi descubrimiento")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNode(hasSetTextAction()).assertTextContains("Mi descubrimiento")
        compose.onNode(hasSetTextAction()).performTextReplacement("a".repeat(1001))
        compose.onNode(hasSetTextAction()).assertTextContains("Mi descubrimiento")
    }
}
