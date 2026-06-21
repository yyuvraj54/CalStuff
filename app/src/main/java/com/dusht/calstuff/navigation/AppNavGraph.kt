package com.dusht.calstuff.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.dusht.calstuff.MainBottomNavBar
import com.dusht.calstuff.ui.screens.addmeal.AddMealScreen
import com.dusht.calstuff.ui.screens.navscreen.home.HomeScreen
import com.dusht.calstuff.ui.screens.navscreen.logs.LogsScreen
import com.dusht.calstuff.ui.screens.navscreen.meals.MealsScreen
import com.dusht.calstuff.ui.screens.navscreen.profile.ProfileScreen
import com.dusht.calstuff.ui.screens.navscreen.profile.ProfileViewModel
import com.dusht.calstuff.ui.screens.onboarding.LoginScreen
import com.dusht.calstuff.ui.screens.onboarding.OnboardingFormScreen
import com.dusht.calstuff.ui.screens.onboarding.PostLoginScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.vm.NutritionViewModel
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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F1EB))) {
        AppNavHostContent(
            innerPadding = PaddingValues(0.dp),
            appNavController = appNavController,
            startDestination = startDestination
        )

        if (showBottomBar) {
            // Add meal FAB
            FloatingActionButton(
                onClick = {
                    appNavController.navigate(AppRoute.AddMeal())
                },
                containerColor = Color(0xFFFFD643),
                contentColor = Color(0xFF222222),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 90.dp)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add meal"
                )
            }

            // Bottom nav bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                MainBottomNavBar(appNavController = appNavController)
            }
        }
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

        composable<AppRoute.Meals> {
            MealsScreen()
        }

        composable<AppRoute.Profile> {
            LogsScreen()
        }

        composable<AppRoute.ProfileTab> {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateToLogin = {
                    appNavController.navigateAndClearBackStack(AppRoute.Login)
                },
            )
        }

        composable<AppRoute.UserDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<AppRoute.UserDetail>()
            androidx.compose.material3.Text("User Detail: ${args.userId}")
        }

        composable<AppRoute.AddMeal> { backStackEntry ->
            val args = backStackEntry.toRoute<AppRoute.AddMeal>()
            val nutritionViewModel: NutritionViewModel = hiltViewModel()
            val nutritionState by nutritionViewModel.state.collectAsStateWithLifecycle()
            val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
            val initialDay = if (args.dayOfMonth == 0) today else args.dayOfMonth

            AddMealScreen(
                onBack = { appNavController.navigateUp() },
                initialDay = initialDay,
                editableWindowDays = nutritionState.editableWindowDays,
                onSave = { day, meal ->
                    // Convert each FoodItem to a MealLogEntry and add to ViewModel
                    if (meal.items.isNotEmpty()) {
                        meal.items.forEach { item ->
                            nutritionViewModel.addMealForDay(
                                day,
                                com.dusht.calstuff.ui.model.MealLogEntry(
                                    name = item.name,
                                    mealType = item.mealType,
                                    calories = item.calories,
                                    protein = item.protein,
                                    carbs = item.carbs,
                                    fat = item.fat,
                                    time = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                                        .format(java.util.Date())
                                )
                            )
                        }
                    } else {
                        // Single meal entry (no items list)
                        nutritionViewModel.addMealForDay(
                            day,
                            com.dusht.calstuff.ui.model.MealLogEntry(
                                name = meal.foodName,
                                mealType = meal.mealType,
                                calories = meal.calories,
                                protein = meal.protein,
                                carbs = meal.carbs,
                                fat = meal.fat,
                                time = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                                    .format(java.util.Date())
                            )
                        )
                    }
                    AppLogger.app(
                        message = "Meal saved",
                        extras = mapOf("food" to meal.foodName, "calories" to meal.calories, "day" to day)
                    )
                }
            )
        }

        composable<AppRoute.Settings> {
            androidx.compose.material3.Text("Settings Screen")
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
