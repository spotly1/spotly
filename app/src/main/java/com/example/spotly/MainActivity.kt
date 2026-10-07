package com.example.spotly

import android.os.Bundle
import androidx.compose.runtime.CompositionLocalProvider
import com.example.spotly.ui.components.LocalAddressRepository
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.example.spotly.navigation.SpotlyNavigation
import com.example.spotly.ui.theme.SpotlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val container = (application as SpotlyApplication).container
            CompositionLocalProvider(LocalAddressRepository provides container.addressRepository) {
                SpotlyTheme { SpotlyNavigation(container) }
            }
        }
    }
}
