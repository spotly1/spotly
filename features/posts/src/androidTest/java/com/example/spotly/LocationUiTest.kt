package com.example.spotly

import com.example.spotly.core.ui.R

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.spotly.ui.theme.SpotlyTheme
import com.example.spotly.domain.model.LocationPoint
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
            com.example.spotly.ui.components.LocationAddressButton(LocationPoint(-34.6037, -58.3816), address)
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

}
