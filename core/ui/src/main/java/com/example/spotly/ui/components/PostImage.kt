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
import coil3.request.ImageRequest
import coil3.request.CachePolicy
import androidx.compose.ui.platform.LocalContext
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
    val online = LocalOnline.current
    // El detalle offline reutiliza la variante de 1080 px que ya cargó el feed.
    val pixels = if (!online && maxPixels > 1080) 1080 else maxPixels
    val url = remember(model, pixels) { optimizedPostImageUrl(model, pixels) }
    val context = LocalContext.current
    val request = remember(context, url, online) {
        ImageRequest.Builder(context).data(url)
            .networkCachePolicy(if (online) CachePolicy.ENABLED else CachePolicy.DISABLED).build()
    }
    var attempt by remember(url) { mutableIntStateOf(0) }
    var loading by remember(url, attempt) { mutableStateOf(true) }
    var failed by remember(url, attempt) { mutableStateOf(false) }
    Box(modifier, contentAlignment = Alignment.Center) {
        key(url, attempt, online) {
            AsyncImage(
                model = request, contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = contentScale,
                onLoading = { loading = true; failed = false },
                onSuccess = { loading = false; failed = false },
                onError = { loading = false; failed = true }
            )
        }
        if (loading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        if (failed) {
            if (online) TextButton(onClick = { attempt++ }) { Text(stringResource(R.string.retry)) }
            else Text(stringResource(R.string.offline_image_missing), Modifier.padding(16.dp))
        }
    }
}
