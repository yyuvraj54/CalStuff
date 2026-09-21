package com.dusht.shared.logic

import com.dusht.shared.model.ActivityLevel
import com.dusht.shared.model.Gender

/**
 * Mifflin–St Jeor equation for TDEE (Total Daily Energy Expenditure), shared so onboarding on
 * both platforms computes the identical daily calorie goal from the same inputs.
 */
object CalorieGoalCalculator {
    const val DEFAULT_CALORIE_GOAL = 2000
    const val MIN_CALORIE_GOAL = 1200
    const val MAX_CALORIE_GOAL = 5000

    /** Returns [DEFAULT_CALORIE_GOAL] if any required input is missing. */
    fun compute(
        gender: Gender?,
        ageYears: Int?,
        heightCm: Float?,
        weightKg: Float?,
        activityLevel: ActivityLevel?,
    ): Int {
        val weight = weightKg ?: return DEFAULT_CALORIE_GOAL
        val height = heightCm ?: return DEFAULT_CALORIE_GOAL
        val age = ageYears ?: return DEFAULT_CALORIE_GOAL
        val bmr = if (gender == Gender.MALE) {
            10f * weight + 6.25f * height - 5f * age + 5f
        } else {
            10f * weight + 6.25f * height - 5f * age - 161f
        }
        val multiplier = activityLevel?.metabolicMultiplier ?: ActivityLevel.SEDENTARY.metabolicMultiplier
        return (bmr * multiplier).toInt().coerceIn(MIN_CALORIE_GOAL, MAX_CALORIE_GOAL)
    }
}
