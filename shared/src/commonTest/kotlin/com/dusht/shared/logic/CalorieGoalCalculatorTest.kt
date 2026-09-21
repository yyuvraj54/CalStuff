package com.dusht.shared.logic

import com.dusht.shared.model.ActivityLevel
import com.dusht.shared.model.Gender
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalorieGoalCalculatorTest {

    @Test
    fun missingWeight_returnsDefault() {
        val result = CalorieGoalCalculator.compute(
            gender = Gender.MALE, ageYears = 30, heightCm = 180f, weightKg = null,
            activityLevel = ActivityLevel.MODERATE,
        )
        assertEquals(CalorieGoalCalculator.DEFAULT_CALORIE_GOAL, result)
    }

    @Test
    fun missingHeight_returnsDefault() {
        val result = CalorieGoalCalculator.compute(
            gender = Gender.MALE, ageYears = 30, heightCm = null, weightKg = 75f,
            activityLevel = ActivityLevel.MODERATE,
        )
        assertEquals(CalorieGoalCalculator.DEFAULT_CALORIE_GOAL, result)
    }

    @Test
    fun missingAge_returnsDefault() {
        val result = CalorieGoalCalculator.compute(
            gender = Gender.MALE, ageYears = null, heightCm = 180f, weightKg = 75f,
            activityLevel = ActivityLevel.MODERATE,
        )
        assertEquals(CalorieGoalCalculator.DEFAULT_CALORIE_GOAL, result)
    }

    @Test
    fun maleModerateActivity_matchesKnownMifflinStJeorValue() {
        // BMR = 10*75 + 6.25*180 - 5*30 + 5 = 750 + 1125 - 150 + 5 = 1730
        // TDEE = 1730 * 1.55 = 2681.5 -> 2681
        val result = CalorieGoalCalculator.compute(
            gender = Gender.MALE, ageYears = 30, heightCm = 180f, weightKg = 75f,
            activityLevel = ActivityLevel.MODERATE,
        )
        assertEquals(2681, result)
    }

    @Test
    fun femaleSedentaryActivity_matchesKnownMifflinStJeorValue() {
        // BMR = 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 - 161 = 1345.25
        // TDEE = 1345.25 * 1.2 = 1614.3 -> 1614
        val result = CalorieGoalCalculator.compute(
            gender = Gender.FEMALE, ageYears = 25, heightCm = 165f, weightKg = 60f,
            activityLevel = ActivityLevel.SEDENTARY,
        )
        assertEquals(1614, result)
    }

    @Test
    fun nonMaleGender_usesFemaleFormula() {
        // OTHER/PREFER_NOT_SAY fall through to the "else" branch, same as FEMALE.
        val other = CalorieGoalCalculator.compute(
            gender = Gender.OTHER, ageYears = 25, heightCm = 165f, weightKg = 60f,
            activityLevel = ActivityLevel.SEDENTARY,
        )
        val female = CalorieGoalCalculator.compute(
            gender = Gender.FEMALE, ageYears = 25, heightCm = 165f, weightKg = 60f,
            activityLevel = ActivityLevel.SEDENTARY,
        )
        assertEquals(female, other)
    }

    @Test
    fun nullActivityLevel_defaultsToSedentaryMultiplier() {
        val withNull = CalorieGoalCalculator.compute(
            gender = Gender.MALE, ageYears = 30, heightCm = 180f, weightKg = 75f,
            activityLevel = null,
        )
        val withSedentary = CalorieGoalCalculator.compute(
            gender = Gender.MALE, ageYears = 30, heightCm = 180f, weightKg = 75f,
            activityLevel = ActivityLevel.SEDENTARY,
        )
        assertEquals(withSedentary, withNull)
    }

    @Test
    fun extremeInputs_clampToMinBound() {
        // Very low weight/height/age should still clamp at the floor, never go below it.
        val result = CalorieGoalCalculator.compute(
            gender = Gender.FEMALE, ageYears = 13, heightCm = 100f, weightKg = 35f,
            activityLevel = ActivityLevel.SEDENTARY,
        )
        assertTrue(result >= CalorieGoalCalculator.MIN_CALORIE_GOAL, "expected >= ${CalorieGoalCalculator.MIN_CALORIE_GOAL}, got $result")
    }

    @Test
    fun extremeInputs_clampToMaxBound() {
        val result = CalorieGoalCalculator.compute(
            gender = Gender.MALE, ageYears = 20, heightCm = 250f, weightKg = 300f,
            activityLevel = ActivityLevel.VERY_ACTIVE,
        )
        assertEquals(CalorieGoalCalculator.MAX_CALORIE_GOAL, result)
    }
}
