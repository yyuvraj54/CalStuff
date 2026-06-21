package com.dusht.calstuff.ui.model

import java.util.Calendar

/**
 * Single source of truth for all HomeScreen widget data.
 * All widgets derive their values from this so everything stays in sync.
 */
data class HomeScreenData(
    val dailyCalorieGoal: Int,
    val todayNutrition: DailyNutritionConfig,
    /** Week days excluding today — today is always derived from todayNutrition */
    val pastWeekDays: List<DayProgress>,
    val streakDays: Int,
    val bmiConfig: BmiConfig
) {
    /** Full 7-day week list: pastWeekDays + today derived from todayNutrition (always in sync) */
    val weekDays: List<DayProgress>
        get() {
            val cal = Calendar.getInstance()
            val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val todayIndex = (todayDayOfWeek + 5) % 7 // Monday=0 .. Sunday=6
            val todayDate = cal.get(Calendar.DAY_OF_MONTH)
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            val todayEntry = DayProgress(
                dayName = dayNames[todayIndex],
                date = todayDate.toString(),
                caloriesConsumed = todayNutrition.caloriesConsumed
            )

            // Insert today at the correct index, keep past days before it
            val result = mutableListOf<DayProgress>()
            var pastIdx = 0
            for (i in 0..6) {
                if (i == todayIndex) {
                    result.add(todayEntry)
                } else if (i < todayIndex && pastIdx < pastWeekDays.size) {
                    result.add(pastWeekDays[pastIdx])
                    pastIdx++
                } else {
                    // Future days: no data
                    val dayDate2 = todayDate + (i - todayIndex)
                    result.add(DayProgress(dayNames[i], dayDate2.toString(), 0))
                }
            }
            return result
        }

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

    /** Total calories consumed this month (sum of all days with data) */
    val monthlyCaloriesConsumed: Int
        get() = dayCalorieRatios.values.sumOf { ratio ->
            (ratio * dailyCalorieGoal).toInt()
        }

    /** Monthly calorie goal = daily goal × days in month */
    val monthlyCalorieGoal: Int
        get() {
            val cal = Calendar.getInstance()
            return dailyCalorieGoal * cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

    /**
     * Map of day-of-month → calorieConsumed/calorieGoal ratio for the calendar borders.
     * Only days with actual logged data are included — no data means no border.
     * NOTE: this covers only the current week. The HomeScreen passes the full-month ratios
     * from MonthLogsData.dayCalorieRatios directly to the calendar widget.
     */
    val dayCalorieRatios: Map<Int, Float>
        get() {
            if (dailyCalorieGoal <= 0) return emptyMap()
            val cal = Calendar.getInstance()
            val today = cal.get(Calendar.DAY_OF_MONTH)
            val ratios = mutableMapOf<Int, Float>()
            // This week's actual data only
            for (day in weekDays) {
                val dayNum = day.date.toIntOrNull() ?: continue
                if (day.caloriesConsumed > 0) {
                    ratios[dayNum] = day.caloriesConsumed.toFloat() / dailyCalorieGoal
                }
            }
            // Today from todayNutrition (only if something was logged)
            if (todayNutrition.caloriesConsumed > 0) {
                ratios[today] = todayNutrition.caloriesConsumed.toFloat() / dailyCalorieGoal
            }
            return ratios
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
         * Today's entry in weekDays is always derived from todayNutrition.
         */
        fun mock(): HomeScreenData {
            val calorieGoal = 2200

            // Macros: protein×4 + carbs×4 + fat×9 = 120×4 + 160×4 + 53×9 = 480+640+477 = 1597
            val protein = 120f
            val carbs = 160f
            val fat = 53f
            val caloriesFromMacros = (protein * 4 + carbs * 4 + fat * 9).toInt()

            val todayNutrition = DailyNutritionConfig(
                caloriesConsumed = caloriesFromMacros,
                proteinConsumed = protein,
                proteinGoal = 150f,
                carbsConsumed = carbs,
                carbsGoal = 250f,
                fatConsumed = fat,
                fatGoal = 70f
            )

            // Past days this week (Mon–yesterday), today is derived from todayNutrition
            val cal = Calendar.getInstance()
            val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sun=1 .. Sat=7
            val todayDate = cal.get(Calendar.DAY_OF_MONTH)
            val todayIndex = (todayDayOfWeek + 5) % 7 // Monday=0 .. Sunday=6

            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val mockCalories = listOf(1850, 2100, 1600, 2200, 1950, 1400, 1750)

            val pastWeekDays = (0 until todayIndex).map { index ->
                val dayDate = todayDate - (todayIndex - index)
                DayProgress(
                    dayName = dayNames[index],
                    date = dayDate.toString(),
                    caloriesConsumed = mockCalories[index]
                )
            }

            return HomeScreenData(
                dailyCalorieGoal = calorieGoal,
                todayNutrition = todayNutrition,
                pastWeekDays = pastWeekDays,
                streakDays = 28,
                bmiConfig = BmiConfig.mock()
            )
        }
    }
}
