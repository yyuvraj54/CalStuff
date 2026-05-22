package com.dusht.calstuff.ui.screens.navscreen.home

import java.util.Calendar

/**
 * Single source of truth for all HomeScreen widget data.
 * All widgets derive their values from this so everything stays in sync.
 */
data class HomeScreenData(
    val dailyCalorieGoal: Int,
    val todayNutrition: DailyNutritionConfig,
    val weekDays: List<DayProgress>
) {
    val weeklyConfig: WeeklyProgressConfig
        get() = WeeklyProgressConfig(
            days = weekDays,
            calorieGoal = dailyCalorieGoal
        )

    val calendarPercentage: Int
        get() {
            val cal = Calendar.getInstance()
            val today = cal.get(Calendar.DAY_OF_MONTH)
            val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            return if (totalDays > 0) (today * 100) / totalDays else 0
        }

    val calendarHighlightedDays: Set<Int>
        get() {
            val cal = Calendar.getInstance()
            val today = cal.get(Calendar.DAY_OF_MONTH)
            return (1..today).toSet()
        }

    companion object {
        /**
         * Mock data with a single calorie goal shared across all widgets.
         * Today's nutrition matches the current day entry in the weekly data.
         */
        fun mock(): HomeScreenData {
            val calorieGoal = 2200

            val todayNutrition = DailyNutritionConfig(
                dailyCalorieGoal = calorieGoal,
                caloriesConsumed = 1540,
                proteinConsumed = 118f,
                proteinGoal = 150f,
                carbsConsumed = 180f,
                carbsGoal = 250f,
                fatConsumed = 52f,
                fatGoal = 70f
            )

            // Weekly data — today's entry matches todayNutrition.caloriesConsumed
            val cal = Calendar.getInstance()
            val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sun=1 .. Sat=7
            val todayDate = cal.get(Calendar.DAY_OF_MONTH)

            // Build 7 days ending at today's day-of-week
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val mockCalories = listOf(1850, 2100, 1600, 2200, 1950, 1400, 1750)

            val weekDays = dayNames.mapIndexed { index, name ->
                // Monday = index 0, Sunday = index 6
                // Calendar: Monday=2 .. Sunday=1; convert todayDayOfWeek to 0-based Mon index
                val todayIndex = (todayDayOfWeek + 5) % 7
                val dayDate = todayDate - (todayIndex - index)

                val calories = if (index == todayIndex) {
                    todayNutrition.caloriesConsumed
                } else if (index < todayIndex) {
                    // Past days: use mock data
                    mockCalories[index]
                } else {
                    // Future days: no data
                    0
                }

                DayProgress(
                    dayName = name,
                    date = dayDate.toString(),
                    caloriesConsumed = calories
                )
            }

            return HomeScreenData(
                dailyCalorieGoal = calorieGoal,
                todayNutrition = todayNutrition,
                weekDays = weekDays
            )
        }
    }
}
