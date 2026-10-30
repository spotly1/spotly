package com.example.spotly.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.spotly.core.ui.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.spotly.domain.model.Post
import com.example.spotly.ui.components.PostImage

@Composable
fun ExploreGrid(
    posts: List<Post>,
    hasMore: Boolean,
    loadingMore: Boolean,
    pageError: String?,
    onLoadMore: () -> Unit,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val nearEnd by remember(gridState, posts.size) {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisible != null && lastVisible >= posts.size - 4
        }
    }
    LaunchedEffect(nearEnd, posts.size, hasMore, loadingMore, pageError) {
        if (nearEnd && hasMore && !loadingMore && pageError == null) onLoadMore()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            PostImage(
                model = post.imageUrl,
                contentDescription = post.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onPostClick(post.id) },
                contentScale = ContentScale.Crop,
                maxPixels = 600
            )
        }
        if (loadingMore || pageError != null) {
            item(key = "pagination", span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (loadingMore) {
                        CircularProgressIndicator()
                    } else if (pageError != null) {
                        Text(
                            text = pageError,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = onLoadMore) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}
