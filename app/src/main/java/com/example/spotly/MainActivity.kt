package com.example.spotly

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.spotly.notifications.NotificationTarget
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.ui.components.LocalOnline
import com.example.spotly.ui.components.LocalAddressRepository
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.example.spotly.navigation.SpotlyNavigation
import com.example.spotly.ui.theme.SpotlyTheme

class MainActivity : ComponentActivity() {
    private var notificationTarget by mutableStateOf<NotificationTarget?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationTarget = NotificationTarget.from(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationTarget = NotificationTarget.from(intent)
        enableEdgeToEdge()

        setContent {
            val container = (application as SpotlyApplication).container
            val online by remember { container.connection.observe() }
                .collectAsStateWithLifecycle(initialValue = container.connection.isOnline())
            CompositionLocalProvider(LocalAddressRepository provides container.addressRepository, LocalOnline provides online) {
                SpotlyTheme {
                    SpotlyNavigation(container, notificationTarget) {
                        notificationTarget = null
                        setIntent(Intent(this, MainActivity::class.java))
                    }
                }
            }
        }
    }
}
