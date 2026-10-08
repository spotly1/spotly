
package com.example.spotly.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

@Composable
fun UserAvatar(
    imageUrl: String,
    username: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)

    if (imageUrl.isBlank()) {
        AvatarPlaceholder(avatarModifier)
    } else {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Foto de perfil de $username",
            modifier = avatarModifier,
            contentScale = ContentScale.Crop,
            loading = { AvatarPlaceholder(Modifier.matchParentSize()) },
            error = { AvatarPlaceholder(Modifier.matchParentSize()) }
        )
    }
}

@Composable
private fun AvatarPlaceholder(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
