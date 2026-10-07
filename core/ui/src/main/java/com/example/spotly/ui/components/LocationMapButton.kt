package com.example.spotly.ui.components

import android.annotation.SuppressLint
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.spotly.core.ui.R
import com.example.spotly.domain.model.LocationPoint
import com.example.spotly.domain.model.AppResult
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign

@Composable
fun LocationMapButton(point: LocationPoint) {
    val locale = LocalConfiguration.current.locales[0]
    val repository = LocalAddressRepository.current
    val address by produceState<String?>(null, point, locale) {
        value = null
        value = when (val result = repository?.resolve(point, locale)) {
            is AppResult.Success -> result.data
            is AppResult.Error, null -> null
        }
    }
    LocationAddressButton(point, address)
}

@Composable
fun LocationAddressButton(point: LocationPoint, address: String?) {
    var show by remember(point) { mutableStateOf(false) }
    val label = address ?: stringResource(R.string.location_selected)
    val action = stringResource(R.string.location_map)
    TextButton(
        onClick = { show = true },
        modifier = Modifier.semantics { contentDescription = action },
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isSystemInDarkTheme()) Color(0xFF90CAF9) else Color(0xFF1565C0)
        )
    ) { Text(label, textAlign = TextAlign.Start) }
    if (show) LocationMapDialog(point) { show = false }
}

@SuppressLint("SetJavaScriptEnabled") // HTML propio y Leaflet fijado con SRI; sin puente a código Android.
@Composable
private fun LocationMapDialog(point: LocationPoint, onClose: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val loadingText = stringResource(R.string.map_loading)
    val failedText = stringResource(R.string.map_failed)
    val html = remember(point, loadingText, failedText) {
        context.assets.open("location-map.html").bufferedReader().use { it.readText() }
            .replace("__LAT__", point.latitude.toString())
            .replace("__LON__", point.longitude.toString())
            .replace("__LOADING__", loadingText)
            .replace("__FAILED__", org.json.JSONObject.quote(failedText))
    }
    var failed by remember(point) { mutableStateOf(false) }
    var loading by remember(point) { mutableStateOf(true) }
    val map = remember(html) {
        WebView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.setGeolocationEnabled(false)
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.userAgentString += " Spotly/1.0 (https://github.com/spotly1/spotly)"
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean =
                    true
                override fun onPageFinished(view: WebView?, url: String?) { loading = false }
                override fun onReceivedError(view: WebView?, request: WebResourceRequest, error: WebResourceError?) {
                    if (request.isForMainFrame) { loading = false; failed = true }
                }
                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest, errorResponse: WebResourceResponse?) {
                    if (request.isForMainFrame) { loading = false; failed = true }
                }
            }
            loadDataWithBaseURL("https://spotly.invalid/", html, "text/html", "UTF-8", null)
        }
    }
    DisposableEffect(map) {
        onDispose {
            map.stopLoading()
            (map.parent as? android.view.ViewGroup)?.removeView(map)
            map.destroy()
        }
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column {
                TextButton(onClick = onClose) { Text(stringResource(R.string.map_close)) }
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (failed) {
                    Text(stringResource(R.string.map_failed), Modifier.padding(16.dp))
                }
                TextButton(onClick = { failed = false; loading = true; map.loadDataWithBaseURL("https://spotly.invalid/", html, "text/html", "UTF-8", null) }) { Text(stringResource(R.string.retry)) }
                AndroidView(factory = { map }, modifier = Modifier.fillMaxWidth().weight(1f))
                TextButton(onClick = { uriHandler.openUri("https://www.openstreetmap.org/copyright") }) {
                    Text(stringResource(R.string.map_attribution))
                }
            }
        }
    }
}
