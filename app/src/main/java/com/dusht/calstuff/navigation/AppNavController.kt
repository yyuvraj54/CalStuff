package com.dusht.calstuff.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder

@SuppressLint("CompositionLocalNaming")
val LocalAppNavController = compositionLocalOf<AppNavController?> { null }

class AppNavController(private val navController: NavHostController) {

    fun navigateToBottomDestination(destination: BottomNavDestination) {
        val route: AppRoute = when (destination) {
            BottomNavDestination.HOME -> AppRoute.Home
            BottomNavDestination.AICHAT -> AppRoute.Search
            BottomNavDestination.LOGS -> AppRoute.Profile
        }

        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigate(route: AppRoute, navOptions: NavOptions? = null) {
        navController.navigate(route, navOptions)
    }

    fun navigate(route: AppRoute, builder: NavOptionsBuilder.() -> Unit) {
        navController.navigate(route = route, builder = builder)
    }

    fun navigateAndClearBackStack(route: AppRoute) {
        navController.navigate(route) {
            popUpTo(0) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun getNavController(): NavHostController = navController

    fun navigateUp(): Boolean {
        return navController.navigateUp()
    }

    fun popBackStack(): Boolean {
        return navController.popBackStack()
    }
}
