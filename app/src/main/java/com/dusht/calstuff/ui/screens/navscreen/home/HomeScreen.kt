package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.ui.components.bmi.BmiCard
import com.dusht.calstuff.ui.components.calendar.CalendarProgressCardCompact
import com.dusht.calstuff.ui.components.nutrition.DailyProgressCard
import com.dusht.calstuff.ui.components.streak.StreakCard
import com.dusht.calstuff.ui.components.weekly.WeeklyLineChartCard
import com.dusht.calstuff.ui.model.HomeScreenData
import com.dusht.calstuff.vm.NutritionViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val data = state.homeScreenData
    val cal = remember { Calendar.getInstance() }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Streak (shown when streak > 0)
        if (data.streakDays > 0) {
            StreakCard(streakDays = data.streakDays)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Today's nutrition — full width
        DailyProgressCard(
            config = data.todayNutrition,
            dailyCalorieGoal = data.dailyCalorieGoal
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Compact calendar — drive ratios from real Room month data, not HomeScreenData
        val monthLogs = state.monthLogsData
        CalendarProgressCardCompact(
            percentage = data.calendarPercentage,
            year = year,
            month = month,
            highlightedDays = data.calendarHighlightedDays,
            caloriesConsumed = monthLogs.dayLogs.values.sumOf { it.totalCalories },
            calorieGoal = data.monthlyCalorieGoal,
            dayCalorieRatios = monthLogs.dayCalorieRatios,
            modifier = Modifier.fillMaxWidth(0.5f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Weekly trend line
        WeeklyLineChartCard(config = data.weeklyConfig)

        Spacer(modifier = Modifier.height(12.dp))

        // 5. BMI
        BmiCard(config = data.bmiConfig)

        // Bottom spacing for floating nav bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB)
@Composable
private fun PreviewHomeScreen() {
    HomeScreen()
}
