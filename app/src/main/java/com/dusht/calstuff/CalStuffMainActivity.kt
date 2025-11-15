package com.dusht.calstuff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dusht.calstuff.navigation.AppNavController
import com.dusht.calstuff.navigation.BottomNavDestination
import com.dusht.calstuff.ui.theme.CalStuffTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.dusht.calstuff.navigation.LocalAppNavController
import android.content.Context
import com.dusht.calstuff.navigation.AppNavGraph

class CalStuffMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalStuffTheme {
                val isLoggedIn = checkLoginStatus()
                val hasCompletedOnboarding = checkOnboardingStatus()

                CalStuffApp(
                    isLoggedIn = isLoggedIn,
                    hasCompletedOnboarding = hasCompletedOnboarding
                )
            }
        }
    }

    private fun checkLoginStatus(): Boolean {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_logged_in", false)
    }

    private fun checkOnboardingStatus(): Boolean {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("onboarding_completed", false)
    }
}

@Composable
fun CalStuffApp(
    isLoggedIn: Boolean,
    hasCompletedOnboarding: Boolean
) {
    val navController = rememberNavController()
    val appNavController = remember(navController) { AppNavController(navController) }

    CompositionLocalProvider(LocalAppNavController provides appNavController) {
        AppNavGraph(
            appNavController = appNavController,
            isLoggedIn = isLoggedIn,
            hasCompletedOnboarding = hasCompletedOnboarding,
        )
    }
}


@Composable
fun MainBottomNavBar(appNavController: AppNavController) {
    val navController = appNavController.getNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        BottomNavDestination.entries.forEach { destination ->
            val isSelected = currentDestination?.hasRoute(destination.route) == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    appNavController.navigateToBottomDestination(destination)
                },
                icon = {
                    val iconRes = if (isSelected) {
                        destination.selectedIcon
                    } else {
                        destination.unselectedIcon
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        tint = Color.Unspecified,
                        contentDescription = stringResource(id = destination.titleRes)
                    )
                },
                label = {
                    Text(stringResource(id = destination.titleRes))
                }
            )
        }
    }
}