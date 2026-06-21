package com.dusht.calstuff.vm

import com.dusht.calstuff.ui.model.BmiConfig
import com.dusht.calstuff.ui.model.DailyNutritionConfig
import com.dusht.calstuff.ui.model.DayProgress
import com.dusht.calstuff.ui.model.HomeScreenData
import com.dusht.calstuff.ui.model.MonthLogsData
import java.util.Calendar

data class NutritionUiState(
    val dailyCalorieGoal: Int,
    val monthLogsData: MonthLogsData,
    val bmiConfig: BmiConfig,
    val streakDays: Int,
    /** Number of days (including today) the user can edit. 1 = today only, 2 = today + yesterday, etc. */
    val editableWindowDays: Int = 1
) {
    /** Derive today's nutrition from today's DayLog meals */
    val todayNutrition: DailyNutritionConfig
        get() {
            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val dayLog = monthLogsData.dayLogs[today]
            return if (dayLog != null && dayLog.meals.isNotEmpty()) {
                DailyNutritionConfig(
                    caloriesConsumed = dayLog.totalCalories,
                    proteinConsumed = dayLog.totalProtein,
                    proteinGoal = 150f,
                    carbsConsumed = dayLog.totalCarbs,
                    carbsGoal = 250f,
                    fatConsumed = dayLog.totalFat,
                    fatGoal = 70f
                )
            } else {
                DailyNutritionConfig(
                    caloriesConsumed = 0,
                    proteinConsumed = 0f, proteinGoal = 150f,
                    carbsConsumed = 0f, carbsGoal = 250f,
                    fatConsumed = 0f, fatGoal = 70f
                )
            }
        }

    /** Derive HomeScreenData from the shared state */
    val homeScreenData: HomeScreenData
        get() {
            val cal = Calendar.getInstance()
            val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val todayIndex = (todayDayOfWeek + 5) % 7 // Monday=0..Sunday=6
            val todayDate = cal.get(Calendar.DAY_OF_MONTH)
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            val pastWeekDays = (0 until todayIndex).map { index ->
                val dayDate = todayDate - (todayIndex - index)
                val dayLog = monthLogsData.dayLogs[dayDate]
                DayProgress(
                    dayName = dayNames[index],
                    date = dayDate.toString(),
                    caloriesConsumed = dayLog?.totalCalories ?: 0
                )
            }

            return HomeScreenData(
                dailyCalorieGoal = dailyCalorieGoal,
                todayNutrition = todayNutrition,
                pastWeekDays = pastWeekDays,
                streakDays = streakDays,
                bmiConfig = bmiConfig
            )
        }

    companion object {
        /** Real initial state — no data until Room/Firestore loads. */
        fun empty(): NutritionUiState {
            val cal = Calendar.getInstance()
            return NutritionUiState(
                dailyCalorieGoal = 2000,
                monthLogsData = MonthLogsData(
                    year = cal.get(Calendar.YEAR),
                    month = cal.get(Calendar.MONTH) + 1,
                    calorieGoal = 2000,
                    dayLogs = emptyMap(),
                ),
                bmiConfig = BmiConfig.mock(),
                streakDays = 0,
                editableWindowDays = 2,
            )
        }
    }
}
