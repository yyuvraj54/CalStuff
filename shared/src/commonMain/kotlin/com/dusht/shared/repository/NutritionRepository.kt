package com.dusht.shared.repository

import com.dusht.shared.model.DayNutrition
import com.dusht.shared.model.MealEntry
import kotlinx.coroutines.flow.Flow

/**
 * Contract for reading and writing daily nutrition logs.
 *
 * Firestore layout (per user):
 *   users/{uid}/nutrition/{YYYY-MM-DD}
 *     date: String          "2026-06-13"
 *     calorieGoal: Int
 *     meals: List<MealEntryDto>
 *
 * Implementations:
 *  - Android/Firebase: FirestoreNutritionRepositoryImpl (data module)
 *  - Future custom backend: swap binding in DataModule — no ViewModel changes needed.
 */
interface NutritionRepository {

    /**
     * Live stream of all day-logs for the given year/month.
     * Map key is day-of-month (1–31).
     */
    fun observeMonthLogs(year: Int, month: Int): Flow<Map<Int, DayNutrition>>

    /** Add a new meal to the given day. Creates the day-doc if it doesn't exist. */
    suspend fun addMeal(year: Int, month: Int, day: Int, meal: MealEntry): Result<Unit>

    /** Replace an existing meal (matched by meal.id) in the given day. */
    suspend fun updateMeal(year: Int, month: Int, day: Int, meal: MealEntry): Result<Unit>

    /** Remove a meal by id from the given day. */
    suspend fun deleteMeal(year: Int, month: Int, day: Int, mealId: String): Result<Unit>

    /** Set (or update) the calorie goal for a specific day. */
    suspend fun setDayCalorieGoal(year: Int, month: Int, day: Int, goal: Int): Result<Unit>
}
