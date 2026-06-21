package com.dusht.calstuff.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dusht.calstuff.R
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
@Serializable
sealed interface AppRoute {
    @Serializable
    data object Login : AppRoute

    @Serializable
    data object PostLogin : AppRoute

    @Serializable
    data object OnboardingForm : AppRoute

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Meals : AppRoute

    @Serializable
    data object Profile : AppRoute

    @Serializable
    data class UserDetail(val userId: String) : AppRoute

    @Serializable
    data object ProfileTab : AppRoute

    @Serializable
    data class AddMeal(val dayOfMonth: Int = 0) : AppRoute

    @Serializable
    data object Settings : AppRoute
}

enum class BottomNavDestination(
    @StringRes val titleRes: Int,
    val route: KClass<out AppRoute>,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int
) {
    HOME(
        titleRes = R.string.home,
        route = AppRoute.Home::class,
        selectedIcon = R.drawable.home_icon,
        unselectedIcon = R.drawable.home_icon
    ),
    MEALS(
        titleRes = R.string.meals,
        route = AppRoute.Meals::class,
        selectedIcon = R.drawable.aichat_icon,
        unselectedIcon = R.drawable.aichat_icon
    ),
    LOGS(
        titleRes = R.string.logs,
        route = AppRoute.Profile::class,
        selectedIcon = R.drawable.logs_icon,
        unselectedIcon = R.drawable.logs_icon
    ),
    PROFILE(
        titleRes = R.string.profile_tab,
        route = AppRoute.ProfileTab::class,
        selectedIcon = R.drawable.profile_icon,
        unselectedIcon = R.drawable.profile_icon
    )
}