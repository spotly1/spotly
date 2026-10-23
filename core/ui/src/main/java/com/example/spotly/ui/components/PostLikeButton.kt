package com.example.spotly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.spotly.core.ui.R
import com.example.spotly.ui.state.LikeUiState

@Composable
fun PostLikeButton(state: LikeUiState, onClick: () -> Unit, onRetry: () -> Unit,
    onCountClick: () -> Unit, enabled: Boolean = true) {
    val online = LocalOnline.current
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val liked = state.summary?.likedByMe == true
            IconToggleButton(checked = liked, onCheckedChange = { onClick() },
                modifier = Modifier.size(36.dp),
                enabled = enabled && online && state.summary != null && !state.busy) {
                Icon(if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(if (liked) R.string.remove_like else R.string.add_like),
                    tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            }
            val countDescription = stringResource(R.string.see_likes, state.summary?.count ?: 0)
            Text(state.summary?.count?.toString() ?: stringResource(R.string.count_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable(enabled = enabled && online && state.summary != null,
                    role = Role.Button, onClick = onCountClick)
                    .semantics { contentDescription = countDescription })
            if (state.busy) CircularProgressIndicator(Modifier.padding(start = 8.dp).size(16.dp), strokeWidth = 2.dp)
        }
        state.error?.takeIf { online }?.let {
            Text(it.localizedMessage(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onRetry, enabled = enabled && !state.busy) { Text(stringResource(R.string.retry)) }
        }
    }
}
