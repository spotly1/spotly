
package com.example.spotly.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.spotly.core.ui.R
import com.example.spotly.ui.components.localizedMessage
import com.example.spotly.viewmodel.ExploreUiState
import com.example.spotly.viewmodel.SearchUiState
import androidx.compose.ui.focus.onFocusChanged
import com.example.spotly.domain.model.User

@Composable
fun ExploreScreen(
    exploreState: ExploreUiState,
    searchState: SearchUiState,
    recentUsers: List<User>,
    onQueryChange: (String) -> Unit,
    onSearchRetry: () -> Unit,
    onExploreRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onClearRecent: () -> Unit
) {

    var searching by rememberSaveable {
        mutableStateOf(false)
    }

    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (searching) 4.dp else 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (searching) {
                IconButton(
                    onClick = {
                        keyboard?.hide()
                        focusManager.clearFocus()
                        onQueryChange("")
                        searching = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }

            OutlinedTextField(
                value = searchState.query,
                onValueChange = {
                    searching = true
                    onQueryChange(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            searching = true
                        }
                    },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                placeholder = {
                    Text(stringResource(R.string.search_username))
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchState.query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChange("") }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(
                                    R.string.search_clear
                                )
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboard?.hide()
                    }
                )
            )
        }

        if (searching) {
            SearchResults(
                state = searchState,
                recentUsers = recentUsers,
                onRetry = onSearchRetry,
                onUserClick = { uid ->
                    keyboard?.hide()
                    onUserClick(uid)
                },
                onRemoveRecent = onRemoveRecent,
                onClearRecent = onClearRecent,
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            )


        } else {
            when (val current = exploreState) {

                ExploreUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ExploreUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = current.error.localizedMessage(),
                            color = MaterialTheme.colorScheme.error
                        )

                        TextButton(onClick = onExploreRetry) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }

                is ExploreUiState.Success -> {
                    if (current.posts.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.explore_empty),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            TextButton(onClick = onExploreRetry) {
                                Text(stringResource(R.string.explore_refresh))
                            }
                        }
                    } else {
                        ExploreGrid(
                            posts = current.posts,
                            hasMore = current.hasMore,
                            loadingMore = current.loadingMore,
                            pageError = current.pageError?.localizedMessage(),
                            onLoadMore = onLoadMore,
                            onPostClick = onPostClick
                        )
                    }
                }
            }
        }
    }
}
