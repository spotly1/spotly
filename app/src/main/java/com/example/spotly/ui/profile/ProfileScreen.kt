package com.example.spotly.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import com.example.spotly.viewmodel.ProfileUiState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.spotly.ui.components.PostImage
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.R
import com.example.spotly.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadCurrentUserProfile() }
    ProfileContent(uiState, onEditProfileClick, onLogoutClick, viewModel::loadCurrentUserProfile)
}

@Composable
internal fun ProfileContent(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRetryPosts: () -> Unit
) {
    var selectedPostId by rememberSaveable { mutableStateOf<String?>(null) }
    val gridState = rememberLazyGridState()
    val selected = selectedPostId
    if (selected != null) {
        BackHandler { selectedPostId = null }
        ProfilePostsScreen(uiState, selected, onBack = { selectedPostId = null }, onRetry = onRetryPosts)
        return
    }
    when {
        uiState.isLoading -> LoadingProfile()
        uiState.error != null -> ProfileError(uiState.error?.localizedMessage().orEmpty())
        uiState.user != null -> {
            val currentUser = uiState.user ?: return
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Spacer(Modifier.height(24.dp))
                        if (currentUser.profileImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = currentUser.profileImageUrl,
                                contentDescription = stringResource(R.string.profile_photo),
                                modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = stringResource(R.string.profile_photo),
                                    modifier = Modifier.padding(22.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.profile_username, currentUser.username),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentUser.description.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(currentUser.description, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }

                        Spacer(Modifier.height(24.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ProfileStat(
                                if (uiState.postsLoading || uiState.postsError) stringResource(R.string.count_unavailable)
                                else uiState.posts.size.toString(),
                                stringResource(R.string.posts)
                            )
                            ProfileStat(stringResource(R.string.zero_count), stringResource(R.string.followers))
                            ProfileStat(stringResource(R.string.zero_count), stringResource(R.string.following))
                        }

                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(onEditProfileClick, Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.edit_profile))
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(onLogoutClick, Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.logout))
                        }
                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(24.dp))
                        Text(
                            stringResource(R.string.posts),
                            Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(32.dp))
                        if (uiState.postsLoading) {
                            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                        } else if (uiState.postsError) {
                            Text(stringResource(R.string.error_feed_load), color = MaterialTheme.colorScheme.error)
                            OutlinedButton(onClick = onRetryPosts) { Text(stringResource(R.string.retry)) }
                        } else if (uiState.posts.isEmpty()) Text(
                            stringResource(R.string.no_posts_yet),
                            Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(uiState.posts, key = { it.id }) { post ->
                    PostImage(
                        maxPixels = 480,
                        model = post.imageUrl,
                        contentDescription = post.description,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable(
                            onClickLabel = stringResource(R.string.open_post)
                        ) { selectedPostId = post.id },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingProfile() {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ProfileError(message: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Text(message.ifBlank { stringResource(R.string.profile_load_error) })
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
