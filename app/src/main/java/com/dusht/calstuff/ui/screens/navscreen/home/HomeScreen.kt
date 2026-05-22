package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val data = remember { HomeScreenData.mock() }
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
        // Calendar progress — full width
        CalendarProgressCard(
            percentage = data.calendarPercentage,
            description = "of the monthly plan\ncompleted",
            year = year,
            month = month,
            highlightedDays = data.calendarHighlightedDays
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Compact calendar cards side by side
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalendarProgressCardCompact(
                percentage = data.calendarPercentage,
                year = year,
                month = month,
                highlightedDays = data.calendarHighlightedDays,
                modifier = Modifier.weight(1f)
            )

            // Weekly completion: days with data / 7
            val daysWithData = data.weekDays.count { it.caloriesConsumed > 0 }
            val weeklyPercent = if (daysWithData > 0) (daysWithData * 100) / 7 else 0
            CalendarProgressCardCompact(
                percentage = weeklyPercent,
                year = year,
                month = month,
                highlightedDays = data.calendarHighlightedDays,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Daily nutrition ring — synced with today's entry
        DailyProgressCard(
            config = data.todayNutrition
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Weekly histogram — uses same calorie goal
        WeeklyProgressCard(
            config = data.weeklyConfig
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Weekly line chart — same data, line view
        WeeklyLineChartCard(
            config = data.weeklyConfig
        )

        // Extra bottom spacing for floating nav bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB)
@Composable
private fun PreviewHomeScreen() {
    HomeScreen()
}
