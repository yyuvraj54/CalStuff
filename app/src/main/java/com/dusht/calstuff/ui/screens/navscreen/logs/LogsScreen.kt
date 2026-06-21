package com.dusht.calstuff.ui.screens.navscreen.logs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.ui.components.calendar.CalendarProgressCard
import com.dusht.calstuff.ui.components.logs.DaySummarySection
import com.dusht.calstuff.vm.NutritionViewModel
import java.util.Calendar

@Composable
fun LogsScreen(
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val data = state.monthLogsData
    val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }

    var selectedDay by remember { mutableIntStateOf(today) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CalendarProgressCard(
            percentage = data.calendarPercentage,
            description = "of the monthly plan\ncompleted",
            year = data.year,
            month = data.month,
            highlightedDays = data.highlightedDays,
            dayCalorieRatios = data.dayCalorieRatios,
            selectedDay = selectedDay,
            onDayClick = { day -> selectedDay = day }
        )

        Spacer(modifier = Modifier.height(20.dp))

        DaySummarySection(
            dayLog = data.dayLogs[selectedDay],
            selectedDay = selectedDay,
            year = data.year,
            month = data.month
        )

        // Bottom spacing for floating nav bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}
