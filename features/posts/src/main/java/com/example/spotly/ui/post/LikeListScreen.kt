package com.example.spotly.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spotly.core.ui.R
import com.example.spotly.ui.components.UserAvatar
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.LikeListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikeListScreen(state: LikeListUiState, onBack: () -> Unit, onUserClick: (String) -> Unit, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.likes_title)) }, windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } })
        when (state) {
            LikeListUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is LikeListUiState.Error -> Column(Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.error.localizedMessage())
                TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            is LikeListUiState.Success -> {
                if (state.users.isEmpty()) Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.likes_empty))
                } else LazyColumn(Modifier.fillMaxSize()) {
                    items(state.users, key = { it.uid }) { user ->
                        Row(Modifier.fillMaxWidth().clickable(role = Role.Button) { onUserClick(user.uid) }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            UserAvatar(imageUrl = user.profileImageUrl, username = user.username)
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.profile_username, user.username), style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (user.description.isNotBlank()) Text(user.description, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
