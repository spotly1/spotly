package com.example.spotly

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.spotly.ui.post.CreatePostRoute
import com.example.spotly.ui.theme.SpotlyTheme
import org.junit.Rule
import org.junit.Test

@org.junit.runner.RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class CreatePostScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun descriptionSurvivesRecreationAndRejectsOverLimit() {
        val restoration = androidx.compose.ui.test.junit4.StateRestorationTester(compose)
        val viewModel = createEditorTestViewModel()
        restoration.setContent { SpotlyTheme { CreatePostRoute(viewModel) } }
        compose.onNode(hasSetTextAction()).performScrollTo().performTextInput("Mi descubrimiento")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNode(hasSetTextAction()).assertTextContains("Mi descubrimiento")
        compose.onNode(hasSetTextAction()).performTextReplacement("a".repeat(1001))
        compose.onNode(hasSetTextAction()).assertTextContains("Mi descubrimiento")
    }
}
