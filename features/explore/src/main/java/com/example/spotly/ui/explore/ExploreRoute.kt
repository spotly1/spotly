
package com.example.spotly.ui.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotly.viewmodel.ExploreViewModel
import com.example.spotly.viewmodel.SearchViewModel

@Composable
fun ExploreRoute(
    exploreViewModel: ExploreViewModel,
    searchViewModel: SearchViewModel,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    val exploreState by exploreViewModel.uiState.collectAsStateWithLifecycle()
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val recentUsers by searchViewModel.recentUsers.collectAsStateWithLifecycle()

    ExploreScreen(
        exploreState = exploreState,
        searchState = searchState,
        recentUsers = recentUsers,
        onQueryChange = searchViewModel::changeQuery,
        onSearchRetry = searchViewModel::retry,
        onExploreRetry = exploreViewModel::refresh,
        onUserClick = { uid ->
            searchViewModel.recordVisitedUser(uid)
            onUserClick(uid)
        },
        onRemoveRecent = searchViewModel::removeRecentUser,
        onClearRecent = searchViewModel::clearRecentUsers,
        onPostClick = onPostClick
    )
}
