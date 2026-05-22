package com.dusht.calstuff.ui.screens.navscreen.home

data class DailyNutritionConfig(
    val dailyCalorieGoal: Int,
    val caloriesConsumed: Int,
    val proteinConsumed: Float,
    val proteinGoal: Float,
    val carbsConsumed: Float,
    val carbsGoal: Float,
    val fatConsumed: Float,
    val fatGoal: Float
) {
    val proteinPercent: Float
        get() = if (proteinGoal + carbsGoal + fatGoal > 0f)
            proteinConsumed / (proteinConsumed + carbsConsumed + fatConsumed).coerceAtLeast(1f)
        else 0f

    val carbsPercent: Float
        get() = if (proteinGoal + carbsGoal + fatGoal > 0f)
            carbsConsumed / (proteinConsumed + carbsConsumed + fatConsumed).coerceAtLeast(1f)
        else 0f

    val fatPercent: Float
        get() = if (proteinGoal + carbsGoal + fatGoal > 0f)
            fatConsumed / (proteinConsumed + carbsConsumed + fatConsumed).coerceAtLeast(1f)
        else 0f

    companion object {
        fun mock() = DailyNutritionConfig(
            dailyCalorieGoal = 2200,
            caloriesConsumed = 1540,
            proteinConsumed = 118f,
            proteinGoal = 150f,
            carbsConsumed = 180f,
            carbsGoal = 250f,
            fatConsumed = 52f,
            fatGoal = 70f
        )
    }
}
