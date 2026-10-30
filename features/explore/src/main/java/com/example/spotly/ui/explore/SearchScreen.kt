
package com.example.spotly.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spotly.core.ui.R
import com.example.spotly.ui.components.UserAvatar
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.SearchUiState
import com.example.spotly.viewmodel.UserSearchState

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onUserClick: (String) -> Unit,
    showSearchInput: Boolean = true
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = if (showSearchInput) 20.dp else 8.dp)
    ) {
        if (showSearchInput) {
            Text(
                stringResource(R.string.search),
                Modifier.padding(vertical = 20.dp),
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                label = { Text(stringResource(R.string.search_username)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                stringResource(R.string.search_clear)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { keyboard?.hide() }
                )
            )

            Text(
                stringResource(R.string.search_hint),
                Modifier.padding(vertical = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (val result = state.result) {
            UserSearchState.Idle -> {
                Text(
                    stringResource(R.string.search_start),
                    Modifier.padding(16.dp)
                )
            }

            UserSearchState.Loading -> {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            is UserSearchState.Error -> {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        result.error.localizedMessage(),
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
                        stringResource(R.string.search_empty),
                        Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(result.users, key = { it.uid }) { user ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    keyboard?.hide()
                                    onUserClick(user.uid)
                                },
                                leadingContent = {
                                    UserAvatar(
                                        user.profileImageUrl,
                                        user.username,
                                        size = 44.dp
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        stringResource(
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
                                            user.description,
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
                                    stringResource(R.string.search_limit),
                                    Modifier.padding(vertical = 12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
