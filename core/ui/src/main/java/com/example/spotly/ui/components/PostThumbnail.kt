package com.example.spotly.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun PostThumbnail(imageUrl: String) {
    val thumbnailUrl = remember(imageUrl) {
        optimizedPostImageUrl(imageUrl, 160)
    }

    AsyncImage(
        model = thumbnailUrl,
        contentDescription = "Foto de la publicación",
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(6.dp)),
        contentScale = ContentScale.Crop
    )
}
