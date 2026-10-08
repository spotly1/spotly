package com.example.spotly.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spotly.domain.model.User
import com.example.spotly.ui.components.PostImage
import com.example.spotly.viewmodel.ProfilePostsState
import com.example.spotly.core.ui.R

@Composable
internal fun ProfileContent(
    user: User,
    postsState: ProfilePostsState,
    isOwnProfile: Boolean,
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onFollowClick: () -> Unit = {},
    isFollowing: Boolean = false,
    onRetryPosts: () -> Unit
) {
    var selectedPostId by rememberSaveable(user.uid) {
        mutableStateOf<String?>(null)
    }

    val gridState = rememberLazyGridState()
    val selected = selectedPostId

    if (selected != null) {
        BackHandler {
            selectedPostId = null
        }

        ProfilePostsScreen(
            postsState = postsState,
            selectedPostId = selected,
            onBack = { selectedPostId = null },
            onRetry = onRetryPosts
        )
        return
    }

    val posts = (postsState as? ProfilePostsState.Success)?.posts.orEmpty()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                ProfileHeader(
                    username = user.username,
                    description = user.description,
                    profileImageUrl = user.profileImageUrl,
                    postsCount = if (postsState is ProfilePostsState.Success) {
                        posts.size.toString()
                    } else {
                        stringResource(R.string.count_unavailable)
                    },
                    followersCount = stringResource(R.string.zero_count),
                    followingCount = stringResource(R.string.zero_count),
                    isOwnProfile = isOwnProfile,
                    onEditProfileClick = onEditProfileClick,
                    onLogoutClick = onLogoutClick,
                    onFollowClick = onFollowClick,
                    isFollowing = isFollowing
                )

                Spacer(Modifier.height(20.dp))

                when (postsState) {
                    ProfilePostsState.Loading -> {
                        CircularProgressIndicator(
                            Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    is ProfilePostsState.Error -> {
                        Text(
                            stringResource(R.string.error_feed_load),
                            color = MaterialTheme.colorScheme.error
                        )

                        OutlinedButton(onClick = onRetryPosts) {
                            Text(stringResource(R.string.retry))
                        }
                    }

                    is ProfilePostsState.Success -> {
                        if (posts.isEmpty()) {
                            Text(
                                stringResource(R.string.no_posts_yet),
                                Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        items(posts, key = { it.id }) { post ->
            PostImage(
                maxPixels = 480,
                model = post.imageUrl,
                contentDescription = post.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable(
                        onClickLabel = stringResource(R.string.open_post)
                    ) {
                        selectedPostId = post.id
                    },
                contentScale = ContentScale.Crop
            )
        }
    }
}