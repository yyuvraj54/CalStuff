package com.dusht.shared.model

/**
 * Backend-agnostic nutrition domain models.
 * mealType stored as a String ("BREAKFAST", "LUNCH", "DINNER", "SNACKS") so
 * the shared module stays free of any UI/enum coupling.
 */
data class MealEntry(
    val id: String,
    val name: String,
    /** "BREAKFAST" | "LUNCH" | "DINNER" | "SNACKS" */
    val mealType: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    /** Display time string, e.g. "8:30 AM" */
    val time: String,
)

data class DayNutrition(
    val year: Int,
    val month: Int,
    val day: Int,
    val calorieGoal: Int,
    val meals: List<MealEntry>,
)
