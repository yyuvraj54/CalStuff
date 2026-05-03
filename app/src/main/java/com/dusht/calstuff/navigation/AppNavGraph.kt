package com.dusht.calstuff.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.dusht.calstuff.MainBottomNavBar
import com.dusht.calstuff.ui.screens.navscreen.chat.AiChatScreen
import com.dusht.calstuff.ui.screens.navscreen.chat.ChatMessage
import com.dusht.calstuff.ui.screens.navscreen.home.HomeScreen
import com.dusht.calstuff.ui.screens.navscreen.logs.LogsScreen
import com.dusht.calstuff.ui.screens.onboarding.LoginScreen
import com.dusht.calstuff.ui.screens.onboarding.OnboardingFormScreen
import com.dusht.calstuff.ui.screens.onboarding.PostLoginScreen
import com.dusht.calstuff.vm.MainViewModel
import com.dusht.core.logging.AppLogger

@Composable
fun AppNavGraph(
    appNavController: AppNavController,
    isLoggedIn: Boolean,
    hasCompletedOnboarding: Boolean
) {
    val navController = appNavController.getNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    DisposableEffect(navController) {
        val listener =
            androidx.navigation.NavController.OnDestinationChangedListener { _, destination, arguments ->
                AppLogger.navigation(
                    message = "destination_changed",
                    extras = mapOf(
                        "route" to (destination.route ?: destination.toString()),
                        "hasArgs" to (arguments != null)
                    )
                )
            }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val showBottomBar = currentDestination?.let { destination ->
        BottomNavDestination.entries.any { destination.hasRoute(it.route) }
    } ?: false

    val startDestination = getStartDestination(isLoggedIn, hasCompletedOnboarding)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MainBottomNavBar(appNavController = appNavController)
            }
        }
    ) { innerPadding ->
        AppNavHostContent(
            innerPadding = innerPadding,
            appNavController = appNavController,
            startDestination = startDestination
        )
    }
}

@Composable
private fun AppNavHostContent(
    innerPadding: PaddingValues,
    appNavController: AppNavController,
    startDestination: AppRoute
) {
    val navController = appNavController.getNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable<AppRoute.Login> {
            LoginScreen(
                onLoginSuccess = {
                    appNavController.navigateAndClearBackStack(AppRoute.PostLogin)
                }
            )
        }

        composable<AppRoute.PostLogin> {
            PostLoginScreen(
                onNavigateToProfileOnboarding = {
                    appNavController.navigateAndClearBackStack(AppRoute.OnboardingForm)
                },
                onNavigateToHome = {
                    appNavController.navigateAndClearBackStack(AppRoute.Home)
                }
            )
        }

        composable<AppRoute.OnboardingForm> {
            OnboardingFormScreen(
                onComplete = {
                    appNavController.navigateAndClearBackStack(AppRoute.Home)
                }
            )
        }

        composable<AppRoute.Home> {
            HomeScreen()
        }

        composable<AppRoute.Search> {
            AiChatScreen(
                showTopBar = false,
                messages = sampleChatMessages(),
                onSendMessage = { msg ->
                    AppLogger.app(
                        message = "Chat send (local demo)",
                        extras = mapOf("length" to msg.length)
                    )
                }
            )
        }

        composable<AppRoute.Profile> {
            val mainViewModel: MainViewModel = hiltViewModel()
            LogsScreen(
                onLogout = {
                    mainViewModel.logout()
                    appNavController.navigateAndClearBackStack(AppRoute.Login)
                }
            )
        }

        composable<AppRoute.UserDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<AppRoute.UserDetail>()
            androidx.compose.material3.Text("User Detail: ${args.userId}")
        }

        composable<AppRoute.Settings> {
            androidx.compose.material3.Text("Settings Screen")
        }
    }
}

private fun sampleChatMessages(): List<ChatMessage> = listOf(
    ChatMessage(
        id = "1",
        message = "Hey there! How are you doing?",
        timestamp = "10:00 AM",
        isSentByUser = false
    ),
    ChatMessage(
        id = "2",
        message = "I'm good! Tracking meals today.",
        timestamp = "10:02 AM",
        isSentByUser = true
    )
)

private fun getStartDestination(isLoggedIn: Boolean, hasCompletedOnboarding: Boolean): AppRoute {
    return when {
        !isLoggedIn -> AppRoute.Login
        !hasCompletedOnboarding -> AppRoute.OnboardingForm
        else -> AppRoute.Home
    }
}
