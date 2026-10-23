
package com.example.spotly.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.example.spotly.ui.components.PostLikeButton
import com.example.spotly.ui.state.LikeUiState
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.FeedUiState
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import com.example.spotly.ui.components.UserAvatar
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Notifications
import com.example.spotly.domain.model.AppResult
import androidx.compose.material3.TopAppBarDefaults
import com.example.spotly.ui.components.CollapsingTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedUiState,
    onRetry: () -> Unit,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onCreatePostClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLike: (String, Boolean) -> Unit,
    onRetryLikes: (String) -> Unit,
    onLikesClick: (String) -> Unit,
    unreadNotifications: AppResult<Boolean>?,
    onNotificationsClick: () -> Unit,
    explore: Boolean = false,
    onLoadMore: () -> Unit = {}
) {
    CollapsingTopAppBar(
        title = stringResource(if (explore) R.string.explore else R.string.home),
        actions = {
            if (explore) IconButton(onClick = onRetry, enabled = state != FeedUiState.Loading) {
                Icon(Icons.Default.Refresh, stringResource(R.string.explore_refresh))
            }
            val hasUnread = (unreadNotifications as? AppResult.Success)?.data == true
            val noticeError = unreadNotifications is AppResult.Error && com.example.spotly.ui.components.LocalOnline.current
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(badge = {
                    if (hasUnread) Badge()
                    else if (noticeError) Badge { Text("!") }
                }) {
                    Icon(Icons.Outlined.Notifications, stringResource(when {
                        noticeError -> R.string.notifications_load_failed
                        hasUnread -> R.string.notifications_new
                        else -> R.string.notifications
                    }))
                }
            }
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
                    if (state.isCached) item {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            Text(stringResource(R.string.feed_cached), style = MaterialTheme.typography.bodySmall)
                            state.refreshError?.let {
                                Text(it.localizedMessage(), color = MaterialTheme.colorScheme.error)
                                TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                            }
                        }
                    }
                    if (state.posts.isEmpty() && (!explore || !state.hasMore)) {
                        item {
                            Column(
                                Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(stringResource(if (explore) R.string.explore_empty
                                    else if (state.hasFollowing) R.string.feed_following_empty else R.string.feed_no_following),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                if (explore) Text(stringResource(R.string.explore_empty_description),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Button(onClick = onSearchClick) { Text(stringResource(R.string.search_people)) }
                            }
                        }
                    }

                    items(state.posts, key = { it.id }) { post ->
                        val likes = state.likes[post.id] ?: LikeUiState()

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
                                            onDoubleClick = { onLike(post.id, true) }
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
                                    PostLikeButton(likes,
                                        onClick = { onLike(post.id, likes.summary?.likedByMe != true) },
                                        onRetry = { onRetryLikes(post.id) },
                                        onCountClick = { onLikesClick(post.id) })

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
                    if (explore && state.hasMore) item(key = "explore_more") {
                        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            state.pageError?.let { Text(it.localizedMessage(), color = MaterialTheme.colorScheme.error) }
                            if (state.loadingMore) CircularProgressIndicator()
                            else OutlinedButton(onClick = onLoadMore) {
                                Text(stringResource(if (state.pageError == null) R.string.explore_more else R.string.retry))
                            }
                        }
                    }
                }
            }
        }
    }
}
