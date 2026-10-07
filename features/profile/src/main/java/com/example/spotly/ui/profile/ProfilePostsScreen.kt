package com.example.spotly.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spotly.ui.components.PostImage
import com.example.spotly.ui.components.LocationMapButton
import com.example.spotly.core.ui.R
import com.example.spotly.viewmodel.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfilePostsScreen(
    state: ProfileUiState,
    selectedPostId: String,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.posts)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_to_profile))
                }
            }
        )
        when {
            state.postsLoading || state.isLoading -> CircularProgressIndicator(Modifier.padding(20.dp))
            state.postsError || state.error != null -> Column(Modifier.padding(20.dp)) {
                Text(stringResource(R.string.error_feed_load))
                OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            state.posts.isEmpty() -> Text(stringResource(R.string.no_posts_yet), Modifier.padding(20.dp))
            else -> {
                val listState = rememberLazyListState(
                    initialFirstVisibleItemIndex = state.posts.indexOfFirst { it.id == selectedPostId }.coerceAtLeast(0)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(), state = listState,
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.posts, key = { it.id }) { post ->
                        Card(Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.profile_username, post.username),
                                Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                            PostImage(
                                maxPixels = 1600,
                                model = post.imageUrl, contentDescription = post.description,
                                modifier = Modifier.fillMaxWidth().height(360.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(post.description, Modifier.padding(16.dp))
                            post.location?.let { LocationMapButton(it) }
                        }
                    }
                }
            }
        }
    }
}
