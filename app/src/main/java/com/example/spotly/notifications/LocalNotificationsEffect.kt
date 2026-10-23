package com.example.spotly.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.content.ContextCompat
import com.example.spotly.core.ui.R

@Composable
fun LocalNotificationsEffect(controller: LocalNotificationController, uid: String?) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(controller, uid, lifecycle) {
        controller.retainAccount(uid)
        if (uid != null) lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            controller.observeWhileVisible(uid)
        }
    }
    if (uid != null) NotificationPermissionPrompt()
}

@Composable
private fun NotificationPermissionPrompt() {
    if (Build.VERSION.SDK_INT < 33) return
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("notification_preferences", Context.MODE_PRIVATE) }
    var show by remember { mutableStateOf(!preferences.getBoolean("offered", false) &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun dismiss() {
        preferences.edit().putBoolean("offered", true).apply()
        show = false
    }
    if (show) AlertDialog(
        onDismissRequest = { dismiss() },
        title = { Text(stringResource(R.string.local_notifications_permission_title)) },
        text = { Text(stringResource(R.string.local_notifications_permission_body)) },
        confirmButton = { TextButton(onClick = {
            dismiss()
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }) { Text(stringResource(R.string.local_notifications_enable)) } },
        dismissButton = { TextButton(onClick = { dismiss() }) {
            Text(stringResource(R.string.local_notifications_later))
        } }
    )
}

fun openNotificationSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
}
