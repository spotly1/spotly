
package com.example.spotly.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.spotly.core.ui.R
import com.example.spotly.ui.components.LocationMapButton
import com.example.spotly.ui.components.PostImage
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.FeedUiState
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import com.example.spotly.ui.components.UserAvatar
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.TopAppBarDefaults
import com.example.spotly.ui.components.CollapsingTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedUiState,
    onRetry: () -> Unit,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onCreatePostClick: () -> Unit
) {
    CollapsingTopAppBar(
        title = stringResource(R.string.home),
        actions = {
            IconButton(onClick = onCreatePostClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.create_post),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { topPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topPadding.calculateTopPadding() + 8.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            when (state) {
                FeedUiState.Loading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is FeedUiState.Error -> item {
                    Text(
                        state.error.localizedMessage(),
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }

                is FeedUiState.Success -> {
                    if (state.posts.isEmpty()) {
                        item {
                            Text(stringResource(R.string.no_posts_yet))
                        }
                    }

                    items(state.posts, key = { it.id }) { post ->
                        var isLiked by remember(post.id) {
                            mutableStateOf(false)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    UserAvatar(
                                        imageUrl = post.profileImageUrl,
                                        username = post.username,
                                        size = 40.dp,
                                        modifier = Modifier.clickable {
                                            onUserClick(post.authorId)
                                        }
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "@${post.username}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.clickable {
                                                onUserClick(post.authorId)
                                            }
                                        )

                                        post.location?.let { location ->
                                            LocationMapButton(location)
                                        }
                                    }
                                }

                                PostImage(
                                    model = post.imageUrl,
                                    contentDescription = stringResource(
                                        R.string.post_photo_preview
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClickLabel = stringResource(R.string.open_post),
                                            onClick = { onPostClick(post.id) },
                                            onDoubleClick = { isLiked = true }
                                        ),
                                    contentScale = ContentScale.FillWidth
                                )

                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { isLiked = !isLiked },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isLiked)
                                                    Icons.Filled.Favorite
                                                else
                                                    Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Me gusta",
                                                tint = if (isLiked)
                                                    MaterialTheme.colorScheme.error
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Text(
                                            text = if (isLiked) "26" else "67",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Text(
                                        text = post.description,
                                        modifier = Modifier.fillMaxWidth().clickable(
                                            onClickLabel = stringResource(R.string.open_post)
                                        ) { onPostClick(post.id) },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}