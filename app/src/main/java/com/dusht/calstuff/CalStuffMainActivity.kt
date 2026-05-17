package com.dusht.calstuff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dusht.calstuff.navigation.AppNavController
import com.dusht.calstuff.navigation.AppNavGraph
import com.dusht.calstuff.navigation.BottomNavDestination
import com.dusht.calstuff.navigation.LocalAppNavController
import com.dusht.calstuff.ui.theme.CalStuffTheme
import com.dusht.calstuff.vm.MainViewModel
import com.dusht.core.logging.AppLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalStuffMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.lifecycle(
            message = "CalStuffMainActivity.onCreate",
            extras = mapOf("savedState" to (savedInstanceState != null))
        )
        enableEdgeToEdge()
        setContent {
            CalStuffTheme {
                CalStuffApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppLogger.lifecycle(message = "CalStuffMainActivity.onResume")
    }

    override fun onPause() {
        AppLogger.lifecycle(message = "CalStuffMainActivity.onPause")
        super.onPause()
    }

    override fun onDestroy() {
        AppLogger.lifecycle(message = "CalStuffMainActivity.onDestroy")
        super.onDestroy()
    }
}

@Composable
fun CalStuffApp(
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    var navigationReady by remember { mutableStateOf(false) }
    var hasCompletedOnboarding by remember { mutableStateOf(mainViewModel.hasCompletedOnboarding()) }

    LaunchedEffect(Unit) {
        mainViewModel.syncOnboardingFromFirestoreIfLoggedIn()
        hasCompletedOnboarding = mainViewModel.hasCompletedOnboarding()
        navigationReady = true
    }

    if (!navigationReady) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val appNavController = remember(navController) { AppNavController(navController) }

    CompositionLocalProvider(LocalAppNavController provides appNavController) {
        AppNavGraph(
            appNavController = appNavController,
            isLoggedIn = mainViewModel.isLoggedIn(),
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
