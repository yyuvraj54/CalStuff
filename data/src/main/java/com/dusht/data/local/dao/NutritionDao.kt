package com.dusht.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dusht.data.local.entity.DayLogEntity
import com.dusht.data.local.entity.MealEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {

    // ── Observe (live) ────────────────────────────────────────────────────────

    @Query("SELECT * FROM day_log WHERE uid = :uid AND year = :year AND month = :month")
    fun observeMonthDayLogs(uid: String, year: Int, month: Int): Flow<List<DayLogEntity>>

    /** Excludes soft-deleted meals so they never surface in the UI. */
    @Query("SELECT * FROM meal_entry WHERE uid = :uid AND year = :year AND month = :month AND isDeleted = 0")
    fun observeMonthMeals(uid: String, year: Int, month: Int): Flow<List<MealEntryEntity>>

    // ── Day log writes ────────────────────────────────────────────────────────

    /** Creates the day log only if it doesn't exist yet (preserves existing calorieGoal). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDayLogIfNew(dayLog: DayLogEntity)

    /** Full upsert — used when syncing from Firestore. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayLog(dayLog: DayLogEntity)

    @Query("UPDATE day_log SET calorieGoal = :goal WHERE uid = :uid AND year = :year AND month = :month AND day = :day")
    suspend fun updateCalorieGoal(uid: String, year: Int, month: Int, day: Int, goal: Int)

    @Query("SELECT calorieGoal FROM day_log WHERE uid = :uid AND year = :year AND month = :month AND day = :day")
    suspend fun getCalorieGoalForDay(uid: String, year: Int, month: Int, day: Int): Int?

    // ── Meal writes ───────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntryEntity>)

    /** Marks a meal as soft-deleted and pending sync. */
    @Query("UPDATE meal_entry SET isDeleted = 1, pendingSync = 1, updatedAt = :now WHERE id = :mealId AND uid = :uid")
    suspend fun softDeleteMeal(uid: String, mealId: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM meal_entry WHERE id = :mealId")
    suspend fun hardDeleteMeal(mealId: String)

    @Query("UPDATE meal_entry SET pendingSync = 0 WHERE id = :mealId")
    suspend fun markMealSynced(mealId: String)

    // ── Sync helpers ──────────────────────────────────────────────────────────

    /** Returns meals for a day that haven't been pushed to Firestore yet. */
    @Query("SELECT * FROM meal_entry WHERE uid = :uid AND year = :year AND month = :month AND day = :day AND pendingSync = 1 AND isDeleted = 0")
    suspend fun getPendingMealsForDay(uid: String, year: Int, month: Int, day: Int): List<MealEntryEntity>

    /** Returns all locally-written (pending) meals for a user across all days. */
    @Query("SELECT * FROM meal_entry WHERE uid = :uid AND pendingSync = 1 AND isDeleted = 0")
    suspend fun getAllPendingMeals(uid: String): List<MealEntryEntity>

    /** Returns meals marked for deletion that haven't been removed from Firestore yet. */
    @Query("SELECT * FROM meal_entry WHERE uid = :uid AND isDeleted = 1")
    suspend fun getPendingDeletes(uid: String): List<MealEntryEntity>

    /**
     * Deletes synced (non-pending) visible meals for a day.
     * Called before inserting fresh Firestore data so Firestore acts as authority for synced rows.
     */
    @Query("DELETE FROM meal_entry WHERE uid = :uid AND year = :year AND month = :month AND day = :day AND pendingSync = 0 AND isDeleted = 0")
    suspend fun deleteSyncedMealsForDay(uid: String, year: Int, month: Int, day: Int)

    /** All visible meals for a day (used when building the Firestore push payload). */
    @Query("SELECT * FROM meal_entry WHERE uid = :uid AND year = :year AND month = :month AND day = :day AND isDeleted = 0")
    suspend fun getMealsForDay(uid: String, year: Int, month: Int, day: Int): List<MealEntryEntity>
}
