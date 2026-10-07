package com.example.spotly.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotly.core.ui.R
import com.example.spotly.ui.auth.LoginRoute
import com.example.spotly.ui.auth.RegisterRoute
import com.example.spotly.ui.feed.FeedRoute
import com.example.spotly.ui.post.CreatePostRoute
import com.example.spotly.ui.profile.EditProfileRoute
import com.example.spotly.ui.profile.ProfileRoute
import com.example.spotly.ui.search.SearchScreen
import com.example.spotly.viewmodel.AuthViewModel
import com.example.spotly.viewmodel.ProfileViewModel

private object Route {
    const val Login = "login"
    const val Register = "register"
    const val Feed = "feed"
    const val Search = "search"
    const val CreatePost = "create_post"
    const val Profile = "profile"
    const val EditProfile = "edit_profile"
}

@Composable
fun SpotlyNavigation(container: com.example.spotly.AppContainer) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = container.authFactory)
    val profileViewModel: ProfileViewModel = viewModel(factory = container.profileFactory)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    if (authState.isSessionLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(authState.isAuthenticated) {
        val destination = if (authState.isAuthenticated) Route.Feed else Route.Login
        if (currentRoute != destination) {
            navController.navigate(destination) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (authState.isAuthenticated && currentRoute !in setOf(Route.Login, Route.Register)) {
                NavigationBar {
                    NavigationItem(currentRoute, Route.Feed, Icons.Default.Home, R.string.home) {
                        navController.navigateMain(Route.Feed)
                    }
                    NavigationItem(currentRoute, Route.Search, Icons.Default.Search, R.string.search) {
                        navController.navigateMain(Route.Search)
                    }
                    NavigationItem(currentRoute, Route.CreatePost, Icons.Default.AddCircle, R.string.create_post) {
                        navController.navigateMain(Route.CreatePost)
                    }
                    NavigationItem(currentRoute, Route.Profile, Icons.Default.Person, R.string.profile) {
                        navController.navigateMain(Route.Profile)
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (authState.isAuthenticated) Route.Feed else Route.Login,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Login) {
                LoginRoute(
                    viewModel = authViewModel,
                    onRegisterClick = { navController.navigate(Route.Register) }
                )
            }
            composable(Route.Register) {
                RegisterRoute(
                    viewModel = authViewModel,
                    onLoginClick = { navController.popBackStack() }
                )
            }
            composable(Route.Feed) { FeedRoute(viewModel(factory = container.feedFactory)) }
            composable(Route.Search) { SearchScreen() }
            composable(Route.CreatePost) {
                CreatePostRoute(viewModel = viewModel(factory = container.createPostFactory), onPublished = {
                    navController.navigate(Route.Feed) {
                        popUpTo(Route.Feed) { inclusive = false }
                        launchSingleTop = true
                    }
                })
            }
            composable(Route.Profile) {
                ProfileRoute(
                    viewModel = profileViewModel,
                    onEditProfileClick = { navController.navigate(Route.EditProfile) },
                    onLogoutClick = authViewModel::logout
                )
            }
            composable(Route.EditProfile) {
                EditProfileRoute(
                    viewModel = profileViewModel,
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavigationItem(
    currentRoute: String?,
    route: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    labelRes: Int,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = currentRoute == route,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = stringResource(labelRes)) }
    )
}

private fun androidx.navigation.NavHostController.navigateMain(route: String) {
    navigate(route) {
        popUpTo(Route.Feed) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
