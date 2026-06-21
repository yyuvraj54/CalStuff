package com.dusht.calstuff.ui.model

data class DayProgress(
    val dayName: String,
    val date: String,
    val caloriesConsumed: Int
)

data class WeeklyProgressConfig(
    val days: List<DayProgress>,
    val calorieGoal: Int
) {
    val maxCalories: Int
        get() {
            val maxConsumed = days.maxOfOrNull { it.caloriesConsumed } ?: 0
            return maxConsumed.coerceAtLeast(calorieGoal).coerceAtLeast(1)
        }

    val isEmpty: Boolean
        get() = days.isEmpty() || days.all { it.caloriesConsumed == 0 }

    /** Average calories across days that have data (consumed > 0). Returns 0 if no data. */
    val averageCalories: Int
        get() {
            val daysWithData = days.filter { it.caloriesConsumed > 0 }
            if (daysWithData.isEmpty()) return 0
            return daysWithData.sumOf { it.caloriesConsumed } / daysWithData.size
        }

    /** Weekly goal = daily goal × 7 */
    val weeklyGoal: Int
        get() = calorieGoal * 7

    companion object {
        fun mock(): WeeklyProgressConfig {
            return WeeklyProgressConfig(
                days = listOf(
                    DayProgress("Mon", "12", 1850),
                    DayProgress("Tue", "13", 2100),
                    DayProgress("Wed", "14", 1600),
                    DayProgress("Thu", "15", 2200),
                    DayProgress("Fri", "16", 1950),
                    DayProgress("Sat", "17", 1400),
                    DayProgress("Sun", "18", 1750)
                ),
                calorieGoal = 2200
            )
        }

        fun empty(): WeeklyProgressConfig {
            return WeeklyProgressConfig(
                days = listOf(
                    DayProgress("Mon", "12", 0),
                    DayProgress("Tue", "13", 0),
                    DayProgress("Wed", "14", 0),
                    DayProgress("Thu", "15", 0),
                    DayProgress("Fri", "16", 0),
                    DayProgress("Sat", "17", 0),
                    DayProgress("Sun", "18", 0)
                ),
                calorieGoal = 2200
            )
        }
    }
}
