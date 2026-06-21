package com.dusht.calstuff.ui.components.widgetgrid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dusht.calstuff.ui.components.bmi.BmiCard
import com.dusht.calstuff.ui.components.calendar.CalendarProgressCardCompact
import com.dusht.calstuff.ui.components.nutrition.DailyProgressCard
import com.dusht.calstuff.ui.components.streak.StreakCard
import com.dusht.calstuff.ui.components.weekly.WeeklyLineChartCard
import com.dusht.calstuff.ui.model.HomeScreenData
import com.dusht.calstuff.ui.model.widget.WidgetPlacement
import com.dusht.calstuff.ui.model.widget.WidgetType
import java.util.Calendar

@Composable
fun RenderWidget(
    placement: WidgetPlacement,
    data: HomeScreenData,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)

    when (placement.type) {
        WidgetType.STREAK -> StreakCard(
            streakDays = data.streakDays,
            modifier = modifier
        )

        WidgetType.CALENDAR_COMPACT -> CalendarProgressCardCompact(
            percentage = data.calendarPercentage,
            year = year,
            month = month,
            highlightedDays = data.calendarHighlightedDays,
            modifier = modifier
        )

        WidgetType.DAILY_PROGRESS -> DailyProgressCard(
            config = data.todayNutrition,
            dailyCalorieGoal = data.dailyCalorieGoal,
            modifier = modifier
        )

        WidgetType.WEEKLY_CHART -> WeeklyLineChartCard(
            config = data.weeklyConfig,
            modifier = modifier
        )

        WidgetType.BMI -> BmiCard(
            config = data.bmiConfig,
            modifier = modifier
        )
    }
}
