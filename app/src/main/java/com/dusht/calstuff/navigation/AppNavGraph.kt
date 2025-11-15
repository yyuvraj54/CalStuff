package com.dusht.calstuff.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import com.dusht.calstuff.MainBottomNavBar
import com.dusht.calstuff.ui.screens.onboarding.LoginScreen

@Composable
fun AppNavGraph(
    appNavController: AppNavController,
    isLoggedIn: Boolean,
    hasCompletedOnboarding: Boolean
) {
    val navController = appNavController.getNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
        innerPadding
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
        ) {

            composable<AppRoute.Login> {
                LoginScreen(onLoginSuccess = { appNavController.navigateAndClearBackStack(AppRoute.OnboardingForm)})
            }

            composable<AppRoute.OnboardingForm> {
                Text("Onboarding Form Screen")
                // Replace with: OnboardingFormScreen(
                //     onComplete = {
                //         appNavController.navigateAndClearBackStack(AppRoute.Home)
                //     }
                // )
            }

            // Main App Screens (Bottom Nav)
            composable<AppRoute.Home> {
                Text("Home Screen")
                // Replace with: HomeScreen(
                //     onNavigateToSettings = {
                //         appNavController.navigate(AppRoute.Settings)
                //     }
                // )
            }

            composable<AppRoute.Search> {
                Text("AI Chat Screen")
                // Replace with: AIChatScreen()
            }

            composable<AppRoute.Profile> {
                Text("Logs Screen")
                // Replace with: LogsScreen(
                //     onLogout = {
                //         // Clear user data
                //         appNavController.navigateAndClearBackStack(AppRoute.Login)
                //     }
                // )
            }

            // Detail/Full Screens (No Bottom Nav)
            composable<AppRoute.UserDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<AppRoute.UserDetail>()
                Text("User Detail: ${args.userId}")
                // Replace with: UserDetailScreen(
                //     userId = args.userId,
                //     onNavigateBack = { appNavController.navigateUp() }
                // )
            }

            composable<AppRoute.Settings> {
                Text("Settings Screen")
                // Replace with: SettingsScreen(
                //     onNavigateBack = { appNavController.navigateUp() }
                // )
            }
        }
    }
}
private fun getStartDestination(isLoggedIn: Boolean, hasCompletedOnboarding: Boolean): AppRoute {
    return when {
        !isLoggedIn -> AppRoute.Login
        !hasCompletedOnboarding -> AppRoute.OnboardingForm
        else -> AppRoute.Home
    }
}