package com.example.spotly.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spotly.ui.components.PostImage
import com.example.spotly.ui.components.LocationMapButton
import com.example.spotly.viewmodel.FeedUiState
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.core.ui.R

@Composable
fun FeedScreen(state: FeedUiState, onRetry: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(R.string.home), style = MaterialTheme.typography.headlineSmall) }
        when (state) {
            FeedUiState.Loading -> item { CircularProgressIndicator() }
            is FeedUiState.Error -> item {
                Text(state.error.localizedMessage(), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            is FeedUiState.Success -> {
                if (state.posts.isEmpty()) item { Text(stringResource(R.string.no_posts_yet)) }
                items(state.posts, key = { it.id }) { post ->
                    Card(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.profile_username, post.username),
                            Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                        PostImage(model = post.imageUrl,
                            contentDescription = stringResource(R.string.post_photo_preview),
                            modifier = Modifier.fillMaxWidth().height(300.dp), contentScale = ContentScale.Fit)
                        Text(post.description, Modifier.padding(16.dp))
                        post.location?.let { LocationMapButton(it) }
                    }
                }
            }
        }
    }
}
