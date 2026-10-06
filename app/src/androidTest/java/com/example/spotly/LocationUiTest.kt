package com.example.spotly

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.spotly.ui.post.CreatePostContent
import com.example.spotly.ui.theme.SpotlyTheme
import com.example.spotly.viewmodel.CreatePostUiState
import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LocationUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun addressIsClickableAndFallsBackWithoutCoordinates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var address by mutableStateOf<String?>("Av. Corrientes 1234, Buenos Aires")
        compose.setContent { SpotlyTheme { Surface {
            com.example.spotly.ui.components.LocationAddressButton(GeoPoint(-34.6037, -58.3816), address)
        } } }
        compose.onNodeWithText("Av. Corrientes 1234, Buenos Aires").assertIsDisplayed()
        val preview = compose.onRoot().captureToImage().asAndroidBitmap()
        File(context.getExternalFilesDir(null), "address-review.png").outputStream().use {
            preview.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText("Av. Corrientes 1234, Buenos Aires").performClick()
        compose.onNodeWithText(context.getString(R.string.map_close)).assertIsDisplayed().performClick()
        compose.runOnIdle { address = null }
        compose.onNodeWithText(context.getString(R.string.location_selected)).assertIsDisplayed().assertHasClickAction()
        compose.onNodeWithText("-34.6037", substring = true).assertDoesNotExist()
    }

    @Test fun locationCanBeAddedViewedAndRemoved() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var state by mutableStateOf(CreatePostUiState())
        var requests = 0
        compose.setContent { SpotlyTheme { Surface {
            CreatePostContent(null, "Una plaza", {}, {}, {},
                onLocate = { requests++; state = state.copy(location = GeoPoint(-34.6037, -58.3816)) },
                onRemoveLocation = { state = state.copy(location = null) }, state = state)
        } } }
        compose.onNodeWithText(context.getString(R.string.location_add)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, requests) }
        compose.onNodeWithContentDescription(context.getString(R.string.location_map)).performScrollTo().performClick()
        compose.onNodeWithText(context.getString(R.string.map_close)).assertIsDisplayed()
        compose.waitUntil(30_000) {
            compose.onAllNodes(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.ProgressBarRangeInfo)).fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText(context.getString(R.string.map_failed)).assertDoesNotExist()
        compose.waitUntil(30_000) {
            val frame = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            var colored = 0
            var upperColored = 0
            var lowerColored = 0
            for (x in frame.width / 4 until frame.width * 3 / 4 step 30) {
                for (y in frame.height / 4 until frame.height * 3 / 4 step 30) {
                    val color = frame.getPixel(x, y)
                    if (kotlin.math.abs(android.graphics.Color.red(color) - android.graphics.Color.blue(color)) > 15) {
                        colored++
                        if (y < frame.height / 3) upperColored++
                        if (y > frame.height * 2 / 3) lowerColored++
                    }
                }
            }
            frame.recycle()
            colored > 15 && upperColored > 5 && lowerColored > 5
        }
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(context.getExternalFilesDir(null), "map-review.png").outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
        screenshot.recycle()
        compose.onNodeWithText(context.getString(R.string.map_close)).performClick()
        compose.onNodeWithText(context.getString(R.string.location_remove)).performScrollTo().performClick()
        compose.onNodeWithContentDescription(context.getString(R.string.location_map)).assertDoesNotExist()
        compose.runOnIdle { state = state.copy(locationError = true) }
        compose.onNodeWithText(context.getString(R.string.location_failed)).performScrollTo().assertIsDisplayed()
    }
}
