package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.R
import com.dusht.calstuff.navigation.AppRoute
import com.dusht.calstuff.navigation.LocalAppNavController
import com.dusht.calstuff.ui.components.bmi.BmiCard
import com.dusht.calstuff.ui.components.calendar.CalendarProgressCardCompact
import com.dusht.calstuff.ui.components.nutrition.DailyProgressCard
import com.dusht.calstuff.ui.components.streak.StreakCard
import com.dusht.calstuff.ui.components.weekly.WeeklyLineChartCard
import com.dusht.calstuff.ui.model.HomeScreenData
import com.dusht.calstuff.ui.theme.FontSize
import com.dusht.calstuff.ui.theme.calStuffColors
import com.dusht.calstuff.vm.HomeViewModel
import com.dusht.calstuff.vm.NutritionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val data = state.homeScreenData
    val cal = remember { Calendar.getInstance() }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)

    val displayName by homeViewModel.displayName.collectAsStateWithLifecycle()
    val appNavController = LocalAppNavController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        HomeHeader(
            displayName = displayName,
            onProfileClick = { appNavController?.navigate(AppRoute.ProfileTab) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Streak + compact calendar — always share one row, same height (tallest wins).
        // Streak shows a "start your streak" prompt when there's no active streak yet.
        // Calendar's Card is built on BoxWithConstraints (a SubcomposeLayout), which doesn't
        // support intrinsic measurement, so IntrinsicSize.Max can't be used here — instead we
        // measure each card's natural height and sync the taller one back onto both.
        val monthLogs = state.monthLogsData
        val density = LocalDensity.current
        var streakHeightPx by remember { mutableIntStateOf(0) }
        var calendarHeightPx by remember { mutableIntStateOf(0) }
        val rowHeight = remember(streakHeightPx, calendarHeightPx) {
            val maxPx = maxOf(streakHeightPx, calendarHeightPx)
            if (maxPx > 0) with(density) { maxPx.toDp() } else null
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StreakCard(
                streakDays = data.streakDays,
                modifier = Modifier
                    .weight(1f)
                    .then(rowHeight?.let { Modifier.height(it) } ?: Modifier)
                    .onSizeChanged { streakHeightPx = it.height }
            )
            CalendarProgressCardCompact(
                percentage = data.calendarPercentage,
                year = year,
                month = month,
                highlightedDays = data.calendarHighlightedDays,
                caloriesConsumed = monthLogs.dayLogs.values.sumOf { it.totalCalories },
                calorieGoal = data.monthlyCalorieGoal,
                dayCalorieRatios = monthLogs.dayCalorieRatios,
                modifier = Modifier
                    .weight(1f)
                    .then(rowHeight?.let { Modifier.height(it) } ?: Modifier)
                    .onSizeChanged { calendarHeightPx = it.height }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Today's nutrition — full width
        DailyProgressCard(
            config = data.todayNutrition,
            dailyCalorieGoal = data.dailyCalorieGoal,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Weekly trend line
        WeeklyLineChartCard(config = data.weeklyConfig)

        Spacer(modifier = Modifier.height(12.dp))

        // 4. BMI
        BmiCard(config = data.bmiConfig)

        // Bottom spacing for floating nav bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun HomeHeader(
    displayName: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.calStuffColors
    val todayLabel = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = if (displayName.isNotBlank()) {
                    stringResource(R.string.home_greeting, displayName)
                } else {
                    stringResource(R.string.home)
                },
                color = colors.textPrimary,
                fontSize = FontSize.heading,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = todayLabel,
                color = colors.textSecondary,
                fontSize = FontSize.small,
                fontWeight = FontWeight.Medium,
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.inverseSurface)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.profile_tab),
                tint = colors.onInverseSurface,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB)
@Composable
private fun PreviewHomeScreen() {
    HomeScreen()
}
