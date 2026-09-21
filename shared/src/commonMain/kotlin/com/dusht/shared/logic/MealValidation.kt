package com.dusht.shared.logic

/** Sanity limits for a single logged food item — shared so Android and iOS reject the same inputs. */
object MealLimits {
    const val MAX_CALORIES = 5000
    const val MAX_PROTEIN = 300f
    const val MAX_CARBS = 500f
    const val MAX_FAT = 200f
}

/**
 * Validates a single food item's macro/calorie inputs before it's added to a meal.
 * Returns human-readable error messages; empty means valid.
 */
fun validateFoodItem(
    name: String,
    calories: Int,
    protein: Float,
    carbs: Float,
    fat: Float,
): List<String> {
    val errors = mutableListOf<String>()
    if (name.isBlank()) errors.add("Food name is required")
    if (calories <= 0) errors.add("Calories must be greater than 0")
    if (calories > MealLimits.MAX_CALORIES) errors.add("Calories cannot exceed ${MealLimits.MAX_CALORIES}")
    if (protein > MealLimits.MAX_PROTEIN) errors.add("Protein cannot exceed ${MealLimits.MAX_PROTEIN.toInt()}g")
    if (carbs > MealLimits.MAX_CARBS) errors.add("Carbs cannot exceed ${MealLimits.MAX_CARBS.toInt()}g")
    if (fat > MealLimits.MAX_FAT) errors.add("Fat cannot exceed ${MealLimits.MAX_FAT.toInt()}g")
    // Macros shouldn't exceed calories (1g protein=4cal, 1g carbs=4cal, 1g fat=9cal)
    val macroCalories = (protein * 4 + carbs * 4 + fat * 9).toInt()
    if (macroCalories > calories * 1.5) errors.add("Macros seem too high for the calorie count")
    return errors
}
