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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotly.ui.components.PostImage
import com.example.spotly.ui.components.LocationMapButton
import com.example.spotly.viewmodel.FeedViewModel
import com.example.spotly.viewmodel.FeedUiState
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.R

@Composable
fun FeedScreen(viewModel: FeedViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeedContent(state, viewModel::retry)
}

@Composable
internal fun FeedContent(state: FeedUiState, onRetry: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(R.string.home), style = MaterialTheme.typography.headlineSmall) }
        if (state.isLoading) item { CircularProgressIndicator() }
        state.error?.let { error ->
            item {
                Text(error.localizedMessage(), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
        }
        if (!state.isLoading && state.error == null && state.posts.isEmpty()) {
            item { Text(stringResource(R.string.no_posts_yet)) }
        }
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
