package com.example.spotly.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Explore
import com.example.spotly.ui.feed.ExploreRoute
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
import com.example.spotly.ui.post.PostDetailRoute
import com.example.spotly.ui.post.LikeListRoute
import com.example.spotly.ui.notifications.NotificationsRoute
import com.example.spotly.ui.profile.EditProfileRoute
import com.example.spotly.ui.profile.ProfileRoute
import com.example.spotly.ui.search.SearchRoute
import com.example.spotly.viewmodel.AuthViewModel
import com.example.spotly.viewmodel.ProfileViewModel
import com.example.spotly.ui.profile.UserProfileRoute
import com.example.spotly.ui.profile.FollowListRoute
import com.example.spotly.domain.model.FollowListType
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.example.spotly.ui.components.LocalOnline
import com.example.spotly.ui.components.OfflineBanner
import com.example.spotly.ui.components.OnlineRequired
import com.example.spotly.notifications.NotificationTarget
import com.example.spotly.notifications.LocalNotificationsEffect
import com.example.spotly.notifications.openNotificationSettings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

private object Route {
    const val Login = "login"
    const val Register = "register"
    const val Feed = "feed"
    const val Explore = "explore"
    const val Search = "search"
    const val Notifications = "notifications"
    const val CreatePost = "create_post"
    const val PostDetail = "post/{postId}"
    const val LikeList = "post/{postId}/likes"
    fun likeList(id: String) = "post/${android.net.Uri.encode(id)}/likes"
    fun postDetail(id: String) = "post/${android.net.Uri.encode(id)}"
    const val Profile = "profile"
    const val EditProfile = "edit_profile"

    const val UserProfile = "user_profile/{uid}"
    const val FollowList = "follow_list/{uid}/{type}"
    fun followList(uid: String, type: FollowListType) = "follow_list/${android.net.Uri.encode(uid)}/${type.name}"
    fun userProfile(uid: String) = "user_profile/$uid"
}

@Composable
fun SpotlyNavigation(container: com.example.spotly.AppContainer,
    notificationTarget: NotificationTarget? = null, onNotificationHandled: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = container.authFactory)
    val profileViewModel: ProfileViewModel = viewModel(factory = container.profileFactory)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val online = LocalOnline.current

    if (authState.isSessionLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LocalNotificationsEffect(container.localNotifications,
        if (authState.isAuthenticated) authViewModel.currentUserId() else null)

    LaunchedEffect(authState.isAuthenticated) {
        val destination = if (authState.isAuthenticated) Route.Feed else Route.Login
        if (currentRoute != destination) {
            navController.navigate(destination) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(authState.isAuthenticated, notificationTarget) {
        notificationTarget?.let { target ->
            onNotificationHandled()
            if (authState.isAuthenticated && target.accountId == authViewModel.currentUserId()) {
                val route = if (target.type == com.example.spotly.domain.model.NotificationType.Like)
                    Route.postDetail(target.postId) else Route.userProfile(target.actorId)
                navController.navigate(route) { launchSingleTop = true }
                scope.launch { container.localNotifications.opened(target) }
            }
        }
    }

    Scaffold(
        topBar = { OfflineBanner() },
        bottomBar = {
            if (authState.isAuthenticated && currentRoute !in setOf(Route.Login, Route.Register)) {
                NavigationBar {
                    NavigationItem(currentRoute, Route.Feed, Icons.Default.Home, R.string.home) {
                        navController.navigateMain(Route.Feed)
                    }
                    NavigationItem(currentRoute, Route.Explore, Icons.Default.Explore, R.string.explore) {
                        navController.navigateMain(Route.Explore)
                    }
                    NavigationItem(currentRoute, Route.Search, Icons.Default.Search, R.string.search) {
                        navController.navigateMain(Route.Search)
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
            composable(Route.Feed) {
                FeedRoute(
                    viewModel = viewModel(factory = container.feedFactory),
                    onNotificationsClick = { navController.navigate(Route.Notifications) { launchSingleTop = true } },
                    onLikesClick = { navController.navigate(Route.likeList(it)) { launchSingleTop = true } },
                    onSearchClick = { navController.navigateMain(Route.Search) },
                    onPostClick = { navController.navigate(Route.postDetail(it)) { launchSingleTop = true } },
                    onUserClick = { uid ->
                        if (uid == authViewModel.currentUserId()) {
                            navController.navigateMain(Route.Profile)
                        } else {
                            navController.navigate(Route.userProfile(uid))
                        }
                    },
                    onCreatePostClick = {
                        navController.navigate(Route.CreatePost) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Route.Explore) {
                if (!online) {
                    OnlineRequired { navController.navigateMain(Route.Feed) }
                    return@composable
                }
                ExploreRoute(viewModel = viewModel(factory = container.exploreFactory),
                    onUserClick = { navController.navigate(Route.userProfile(it)) { launchSingleTop = true } },
                    onPostClick = { navController.navigate(Route.postDetail(it)) { launchSingleTop = true } },
                    onCreatePostClick = { navController.navigate(Route.CreatePost) { launchSingleTop = true } },
                    onSearchClick = { navController.navigateMain(Route.Search) },
                    onLikesClick = { navController.navigate(Route.likeList(it)) { launchSingleTop = true } },
                    onNotificationsClick = { navController.navigate(Route.Notifications) { launchSingleTop = true } })
            }
            composable(Route.Notifications) {
                if (!online) {
                    OnlineRequired { navController.navigateMain(Route.Feed) }
                    return@composable
                }
                NotificationsRoute(viewModel = viewModel(factory = container.notificationsFactory),
                    onSettings = { openNotificationSettings(context) },
                    onBack = { navController.popBackStack() },
                    onPostClick = { navController.navigate(Route.postDetail(it)) { launchSingleTop = true } },
                    onUserClick = { uid ->
                        navController.navigate(if (uid == authViewModel.currentUserId()) Route.Profile else Route.userProfile(uid)) {
                            launchSingleTop = true
                        }
                    })
            }
            composable(Route.Search) {
                if (!online) {
                    OnlineRequired { navController.navigateMain(Route.Feed) }
                    return@composable
                }
                SearchRoute(viewModel = viewModel(factory = container.searchFactory), onUserClick = { uid ->
                    if (uid == authViewModel.currentUserId()) navController.navigateMain(Route.Profile)
                    else navController.navigate(Route.userProfile(uid)) { launchSingleTop = true }
                })
            }
            composable(Route.PostDetail) { entry ->
                val postId = entry.arguments?.getString("postId") ?: return@composable
                PostDetailRoute(
                    viewModel = viewModel(factory = container.postDetailFactory(postId)),
                    onLikesClick = { navController.navigate(Route.likeList(postId)) { launchSingleTop = true } },
                    onBack = { navController.popBackStack() },
                    onUserClick = { uid ->
                        if (uid == authViewModel.currentUserId()) {
                            navController.navigateMain(Route.Profile)
                        } else {
                            navController.navigate(Route.userProfile(uid)) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
            composable(Route.LikeList) { entry ->
                if (!online) {
                    OnlineRequired { navController.navigateMain(Route.Feed) }
                    return@composable
                }
                val postId = entry.arguments?.getString("postId") ?: return@composable
                LikeListRoute(viewModel = viewModel(factory = container.likeListFactory(postId)),
                    onBack = { navController.popBackStack() },
                    onUserClick = { uid ->
                        navController.navigate(if (uid == authViewModel.currentUserId()) Route.Profile else Route.userProfile(uid)) {
                            launchSingleTop = true
                        }
                    })
            }
            composable(Route.CreatePost) {
                if (!online) {
                    OnlineRequired { navController.navigateMain(Route.Feed) }
                    return@composable
                }
                CreatePostRoute(
                    viewModel = viewModel(factory = container.createPostFactory),
                    onBack = {
                        navController.popBackStack()
                    },
                    onPublished = {
                        navController.navigate(Route.Feed) {
                            popUpTo(Route.Feed) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Route.Profile) {
                if (!online) {
                    OnlineRequired { navController.navigateMain(Route.Feed) }
                    return@composable
                }
                ProfileRoute(
                    viewModel = profileViewModel,
                    onEditProfileClick = { navController.navigate(Route.EditProfile) },
                    onLogoutClick = authViewModel::logout,
                    onFollowListClick = { uid, type -> navController.navigate(Route.followList(uid, type)) { launchSingleTop = true } },
                    onPostClick = { navController.navigate(Route.postDetail(it)) { launchSingleTop = true } }
                )
            }
            composable(Route.EditProfile) {
                if (!online) {
                    OnlineRequired { navController.navigateMain(Route.Feed) }
                    return@composable
                }
                EditProfileRoute(
                    viewModel = profileViewModel,
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Route.UserProfile) { backStackEntry ->
                if (!online) {
                    OnlineRequired { navController.navigateMain(Route.Feed) }
                    return@composable
                }

                val uid = backStackEntry.arguments?.getString("uid") ?: return@composable

                UserProfileRoute(
                    uid = uid,
                    viewModel = viewModel(factory = container.userProfileFactory),
                    onFollowListClick = { userId, type -> navController.navigate(Route.followList(userId, type)) { launchSingleTop = true } },
                    onPostClick = { navController.navigate(Route.postDetail(it)) { launchSingleTop = true } }
                )
            }
            composable(Route.FollowList) { entry ->
                if (!online) {
                    OnlineRequired { navController.navigateMain(Route.Feed) }
                    return@composable
                }
                val uid = entry.arguments?.getString("uid") ?: return@composable
                val type = FollowListType.entries.firstOrNull { it.name == entry.arguments?.getString("type") } ?: return@composable
                FollowListRoute(type = type, viewModel = viewModel(factory = container.followListFactory(uid, type)),
                    onBack = { navController.popBackStack() },
                    onUserClick = { userId ->
                        // Abrir el perfil conserva la lista en la pila para poder volver atrás.
                        navController.navigate(if (userId == authViewModel.currentUserId()) Route.Profile else Route.userProfile(userId)) {
                            launchSingleTop = true
                        }
                    })
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
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(labelRes),
                modifier = Modifier.size(28.dp)
            )
        }
    )
}

private fun androidx.navigation.NavHostController.navigateMain(route: String) {
    if (currentDestination?.route == route) return

    if (route == Route.Feed && popBackStack(Route.Feed, inclusive = false)) {
        return
    }

    if (route == Route.Profile && popBackStack(Route.Profile, inclusive = false)) {
        return
    }

    navigate(route) {
        popUpTo(Route.Feed) { saveState = true }
        launchSingleTop = true
        restoreState = route != Route.Profile
    }
}
