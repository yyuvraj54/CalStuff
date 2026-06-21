package com.dusht.calstuff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

    val destinations = BottomNavDestination.entries
    val selectedIndex = destinations.indexOfFirst { dest ->
        currentDestination?.hasRoute(dest.route) == true
    }.coerceAtLeast(0)

    val density = LocalDensity.current
    val itemCenters = remember { IntArray(destinations.size) }
    var barHeightPx by remember { mutableIntStateOf(0) }

    val halfPill = with(density) { 22.dp.toPx() } // circle radius = half of 44dp
    val indicatorHeightPx = with(density) { 44.dp.toPx() }

    // Two independent edges — this is what creates the stretch effect
    val leftEdge = remember { Animatable(0f) }
    val rightEdge = remember { Animatable(0f) }

    val edgeSpring = spring<Float>(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)
    val followSpring = spring<Float>(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow / 1.6f)

    LaunchedEffect(selectedIndex) {
        val targetCenter = itemCenters[selectedIndex].toFloat()
        if (targetCenter == 0f) return@LaunchedEffect

        val targetLeft = targetCenter - halfPill
        val targetRight = targetCenter + halfPill

        // First launch — snap both edges
        if (leftEdge.value == 0f && rightEdge.value == 0f) {
            leftEdge.snapTo(targetLeft)
            rightEdge.snapTo(targetRight)
            return@LaunchedEffect
        }

        val currentCenter = (leftEdge.value + rightEdge.value) / 2f
        val movingRight = targetCenter > currentCenter

        // Leading edge moves first (fast), trailing edge follows (slower)
        coroutineScope {
            if (movingRight) {
                // Moving right: right edge leads, left edge follows
                launch { rightEdge.animateTo(targetRight, edgeSpring) }
                launch { leftEdge.animateTo(targetLeft, followSpring) }
            } else {
                // Moving left: left edge leads, right edge follows
                launch { leftEdge.animateTo(targetLeft, edgeSpring) }
                launch { rightEdge.animateTo(targetRight, followSpring) }
            }
        }
    }

    val indicatorColor = Color(0xFFFFD643)
    val selectedContentColor = Color(0xFF1C1B1F)
    val unselectedContentColor = Color(0xFF9E9E9E)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.85f))
    ) {
        Box(
            modifier = Modifier
                .height(56.dp)
                .onGloballyPositioned { coords ->
                    barHeightPx = coords.size.height
                }
                .padding(horizontal = 8.dp)
        ) {
            // Indicator drawn from leftEdge to rightEdge
            val left = leftEdge.value
            val right = rightEdge.value
            val widthDp = with(density) { (right - left).coerceAtLeast(1f).toDp() }
            val verticalOffsetPx = ((barHeightPx - indicatorHeightPx) / 2f).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(x = left.roundToInt(), y = verticalOffsetPx) }
                    .width(widthDp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(indicatorColor)
            )

            // Icons row
            Row(
                modifier = Modifier
                    .height(56.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEachIndexed { index, destination ->
                    val isSelected = index == selectedIndex

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .onGloballyPositioned { coords ->
                                itemCenters[index] =
                                    (coords.positionInParent().x + coords.size.width / 2).toInt()
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                appNavController.navigateToBottomDestination(destination)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val iconRes = if (isSelected) destination.selectedIcon
                            else destination.unselectedIcon
                        Icon(
                            painter = painterResource(iconRes),
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) selectedContentColor else unselectedContentColor,
                            contentDescription = stringResource(id = destination.titleRes)
                        )
                    }
                }
            }
        }
    }
}
