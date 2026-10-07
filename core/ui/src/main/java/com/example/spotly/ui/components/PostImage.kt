package com.example.spotly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.spotly.core.ui.R

internal fun optimizedPostImageUrl(url: String, maxPixels: Int): String {
    val prefix = "https://res.cloudinary.com/iufz7yvd/image/upload/"
    if (!url.startsWith(prefix)) return url
    val size = maxPixels.coerceIn(1, 1600)
    return prefix + "c_limit,w_$size,h_$size,q_auto,f_auto/" + url.removePrefix(prefix)
}

@Composable
fun PostImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    maxPixels: Int = 1080
) {
    val url = remember(model, maxPixels) { optimizedPostImageUrl(model, maxPixels) }
    var attempt by remember(url) { mutableIntStateOf(0) }
    var loading by remember(url, attempt) { mutableStateOf(true) }
    var failed by remember(url, attempt) { mutableStateOf(false) }
    Box(modifier, contentAlignment = Alignment.Center) {
        key(url, attempt) {
            AsyncImage(
                model = url, contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(), contentScale = contentScale,
                onLoading = { loading = true; failed = false },
                onSuccess = { loading = false; failed = false },
                onError = { loading = false; failed = true }
            )
        }
        if (loading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        if (failed) TextButton(onClick = { attempt++ }) { Text(stringResource(R.string.retry)) }
    }
}
