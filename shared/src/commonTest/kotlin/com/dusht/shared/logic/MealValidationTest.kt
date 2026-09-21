package com.dusht.shared.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MealValidationTest {

    @Test
    fun validItem_hasNoErrors() {
        val errors = validateFoodItem(name = "Chicken breast", calories = 300, protein = 40f, carbs = 0f, fat = 8f)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun blankName_isRejected() {
        val errors = validateFoodItem(name = "  ", calories = 300, protein = 10f, carbs = 10f, fat = 10f)
        assertTrue(errors.any { it.contains("name") })
    }

    @Test
    fun zeroCalories_isRejected() {
        val errors = validateFoodItem(name = "Water", calories = 0, protein = 0f, carbs = 0f, fat = 0f)
        assertTrue(errors.any { it.contains("Calories must be greater than 0") })
    }

    @Test
    fun negativeCalories_isRejected() {
        val errors = validateFoodItem(name = "Bad entry", calories = -50, protein = 0f, carbs = 0f, fat = 0f)
        assertTrue(errors.any { it.contains("Calories must be greater than 0") })
    }

    @Test
    fun caloriesOverLimit_isRejected() {
        val errors = validateFoodItem(name = "Huge meal", calories = 6000, protein = 10f, carbs = 10f, fat = 10f)
        assertTrue(errors.any { it.contains("Calories cannot exceed ${MealLimits.MAX_CALORIES}") })
    }

    @Test
    fun proteinOverLimit_isRejected() {
        val errors = validateFoodItem(name = "Protein bomb", calories = 500, protein = 350f, carbs = 0f, fat = 0f)
        assertTrue(errors.any { it.contains("Protein cannot exceed") })
    }

    @Test
    fun carbsOverLimit_isRejected() {
        val errors = validateFoodItem(name = "Carb bomb", calories = 500, protein = 0f, carbs = 600f, fat = 0f)
        assertTrue(errors.any { it.contains("Carbs cannot exceed") })
    }

    @Test
    fun fatOverLimit_isRejected() {
        val errors = validateFoodItem(name = "Fat bomb", calories = 500, protein = 0f, carbs = 0f, fat = 250f)
        assertTrue(errors.any { it.contains("Fat cannot exceed") })
    }

    @Test
    fun macrosWayHigherThanCalories_isRejected() {
        // 50g protein + 50g carbs + 50g fat = 200+200+450 = 850 kcal from macros, but calories says 100.
        val errors = validateFoodItem(name = "Inconsistent", calories = 100, protein = 50f, carbs = 50f, fat = 50f)
        assertTrue(errors.any { it.contains("Macros seem too high") })
    }

    @Test
    fun macrosWithinTolerance_isAccepted() {
        // 25g protein + 25g carbs + 5g fat = 100+100+45 = 245 kcal from macros vs 300 stated — within 1.5x tolerance.
        val errors = validateFoodItem(name = "Reasonable", calories = 300, protein = 25f, carbs = 25f, fat = 5f)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun multipleViolations_reportsAll() {
        val errors = validateFoodItem(name = "", calories = 6000, protein = 400f, carbs = 600f, fat = 300f)
        assertEquals(5, errors.size)
    }
}
