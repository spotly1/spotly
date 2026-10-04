package com.example.spotly.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotly.ui.feed.FeedScreen
import com.example.spotly.ui.post.CreatePostScreen
import com.example.spotly.ui.profile.ProfileScreen
import com.example.spotly.ui.search.SearchScreen
import com.example.spotly.ui.auth.LoginScreen
import com.example.spotly.ui.auth.RegisterScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotly.viewmodel.AuthViewModel

@Composable
fun SpotlyNavigation() {

    val navController = rememberNavController()
    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    val authViewModel: AuthViewModel = viewModel()

    val startDestination = if (authViewModel.isUserLoggedIn()) {
        "feed"
    } else {
        "login"
    }

    Scaffold(
        bottomBar = {

            if (currentRoute != "login" && currentRoute != "register") {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "feed",
                        onClick = {
                            navController.navigate("feed") {
                                popUpTo("feed") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Inicio"
                            )
                        }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "search",
                        onClick = {
                            navController.navigate("search") {
                                popUpTo("feed") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "create_post",
                        onClick = {
                            navController.navigate("create_post") {
                                popUpTo("feed") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Crear publicación"
                            )
                        }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo("feed") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil"
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onRegisterClick = {
                        navController.navigate("register")
                    },
                    onLoginSuccess = {
                        navController.navigate("feed") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onLoginClick = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate("feed") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("feed") {
                FeedScreen()
            }

            composable("search") {
                SearchScreen()
            }

            composable("create_post") {
                CreatePostScreen()
            }

            composable("profile") {
                ProfileScreen(
                    viewModel = authViewModel,
                    onLogoutClick = {
                        authViewModel.logout()

                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}