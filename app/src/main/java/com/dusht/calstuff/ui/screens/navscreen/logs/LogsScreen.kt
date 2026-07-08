package com.dusht.calstuff.ui.screens.navscreen.logs

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.ui.components.calendar.CalendarProgressCard
import com.dusht.calstuff.ui.components.logs.DaySummarySection
import com.dusht.calstuff.ui.theme.FontSize
import com.dusht.calstuff.ui.theme.calStuffColors
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

    val isCurrentMonth = remember(data.year, data.month) {
        val cal = Calendar.getInstance()
        data.year == cal.get(Calendar.YEAR) && data.month == cal.get(Calendar.MONTH) + 1
    }

    var selectedDay by remember { mutableIntStateOf(today) }

    // Selected day belongs to whichever month is browsed; jump to a sensible day when it changes.
    LaunchedEffect(data.year, data.month) {
        selectedDay = if (isCurrentMonth) today else 1
    }

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
            month = data.month - 1, // CalendarProgressCard/MonthCalendarGrid expect 0-based (Calendar.JANUARY = 0)
            highlightedDays = data.highlightedDays,
            dayCalorieRatios = data.dayCalorieRatios,
            selectedDay = selectedDay,
            onDayClick = { day -> selectedDay = day }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MonthNavigationBar(
            isCurrentMonth = isCurrentMonth,
            onPreviousMonth = { viewModel.showPreviousMonth() },
            onNextMonth = { viewModel.showNextMonth() },
            onJumpToCurrentMonth = { viewModel.showCurrentMonth() },
        )

        Spacer(modifier = Modifier.height(20.dp))

        DaySummarySection(
            dayLog = data.dayLogs[selectedDay],
            selectedDay = selectedDay,
            year = data.year,
            month = data.month - 1, // DaySummarySection also expects 0-based for DateFormatSymbols.months[]
        )

        // Bottom spacing for floating nav bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun MonthNavigationBar(
    isCurrentMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToCurrentMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.calStuffColors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonthNavButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month", onClick = onPreviousMonth)

        if (!isCurrentMonth) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.inverseSurface)
                    .clickable(onClick = onJumpToCurrentMonth)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    tint = colors.onInverseSurface,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Back to current month",
                    color = colors.onInverseSurface,
                    fontSize = FontSize.small,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        MonthNavButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month", onClick = onNextMonth)
    }
}

@Composable
private fun MonthNavButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.calStuffColors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
        )
    }
}
