package com.dusht.data.nutrition.dto

import com.dusht.shared.model.DayNutrition
import com.dusht.shared.model.MealEntry
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Firestore POJO for a single meal entry stored inside a DayLogDto.
 */
@IgnoreExtraProperties
data class MealEntryDto(
    val id: String = "",
    val name: String = "",
    val mealType: String = "",
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val time: String = "",
) {
    fun toDomain() = MealEntry(
        id = id, name = name, mealType = mealType,
        calories = calories, protein = protein,
        carbs = carbs, fat = fat, time = time,
    )
}

fun MealEntry.toDto() = MealEntryDto(
    id = id, name = name, mealType = mealType,
    calories = calories, protein = protein,
    carbs = carbs, fat = fat, time = time,
)

/**
 * Firestore POJO for users/{uid}/nutrition/{YYYY-MM-DD}.
 * Document ID is the date string; @DocumentId populates [date] automatically.
 */
@IgnoreExtraProperties
data class DayLogDto(
    @DocumentId val date: String = "",
    val calorieGoal: Int = 2000,
    val meals: List<MealEntryDto> = emptyList(),
) {
    fun toDomain(year: Int, month: Int, day: Int) = DayNutrition(
        year = year, month = month, day = day,
        calorieGoal = calorieGoal,
        meals = meals.map { it.toDomain() },
    )
}
