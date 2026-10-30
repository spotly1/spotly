
package com.example.spotly.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spotly.core.ui.R
import com.example.spotly.domain.model.User
import com.example.spotly.ui.components.UserAvatar
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.SearchUiState
import com.example.spotly.viewmodel.UserSearchState

@Composable
fun SearchResults(
    state: SearchUiState,
    recentUsers: List<User>,
    onRetry: () -> Unit,
    onUserClick: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onClearRecent: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Si no escribió nada, mostrar el historial.
    if (state.query.isBlank()) {
        if (recentUsers.isEmpty()) {
            Text(
                text = stringResource(R.string.search_start),
                modifier = modifier.padding(16.dp)
            )
        } else {
            Column(modifier) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    TextButton(onClick = onClearRecent) {
                        Text("Borrar todo")
                    }
                }

                LazyColumn {
                    items(recentUsers, key = { it.uid }) { user ->
                        ListItem(
                            modifier = Modifier.clickable {
                                onUserClick(user.uid)
                            },
                            leadingContent = {
                                UserAvatar(
                                    imageUrl = user.profileImageUrl,
                                    username = user.username,
                                    size = 44.dp
                                )
                            },
                            headlineContent = {
                                Text(
                                    text = "@${user.username}",
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                if (user.description.isNotBlank()) {
                                    Text(
                                        text = user.description,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        onRemoveRecent(user.uid)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Quitar de recientes"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        return
    }

    // Cuando escribe, mostrar los resultados normales.
    when (val result = state.result) {

        UserSearchState.Idle -> {
            Text(
                text = stringResource(R.string.search_start),
                modifier = modifier.padding(16.dp)
            )
        }

        UserSearchState.Loading -> {
            Column(modifier) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is UserSearchState.Error -> {
            Column(modifier.padding(16.dp)) {
                Text(
                    text = result.error.localizedMessage(),
                    color = MaterialTheme.colorScheme.error
                )

                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }

        is UserSearchState.Success -> {
            if (result.users.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_empty),
                    modifier = modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        result.users,
                        key = { it.uid }
                    ) { user ->
                        ListItem(
                            modifier = Modifier.clickable {
                                onUserClick(user.uid)
                            },
                            leadingContent = {
                                UserAvatar(
                                    imageUrl = user.profileImageUrl,
                                    username = user.username,
                                    size = 44.dp
                                )
                            },
                            headlineContent = {
                                Text(
                                    text = stringResource(
                                        R.string.profile_username,
                                        user.username
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                if (user.description.isNotBlank()) {
                                    Text(
                                        text = user.description,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        )
                    }

                    if (result.users.size == 20) {
                        item {
                            Text(
                                text = stringResource(R.string.search_limit),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
