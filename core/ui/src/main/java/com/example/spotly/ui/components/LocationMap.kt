
package com.example.spotly.ui.components

import android.annotation.SuppressLint
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.spotly.core.ui.R
import com.example.spotly.domain.model.LocationPoint

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocationMap(
    point: LocationPoint,
    modifier: Modifier = Modifier
) {
    if (!LocalOnline.current) {
        Box(modifier, contentAlignment = Alignment.Center) { Text(stringResource(R.string.offline_map)) }
        return
    }
    val context = LocalContext.current
    val loadingText = stringResource(R.string.map_loading)
    val failedText = stringResource(R.string.map_failed)

    val html = remember(point, loadingText, failedText) {
        context.assets.open("location-map.html")
            .bufferedReader()
            .use { it.readText() }
            .replace("__LAT__", point.latitude.toString())
            .replace("__LON__", point.longitude.toString())
            .replace("__LOADING__", loadingText)
            .replace("__FAILED__", org.json.JSONObject.quote(failedText))
    }

    var loading by remember(html) { mutableStateOf(true) }
    var failed by remember(html) { mutableStateOf(false) }

    val webView = remember(html) {
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
            settings.userAgentString += " Spotly/1.0"

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean = true

                override fun onPageFinished(view: WebView?, url: String?) {
                    loading = false
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest,
                    error: WebResourceError?
                ) {
                    if (request.isForMainFrame) {
                        loading = false
                        failed = true
                    }
                }
            }

            loadDataWithBaseURL(
                "https://spotly.invalid/",
                html,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            (webView.parent as? android.view.ViewGroup)
                ?.removeView(webView)
            webView.destroy()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (failed) {
            Text(
                text = failedText,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
    }
}
