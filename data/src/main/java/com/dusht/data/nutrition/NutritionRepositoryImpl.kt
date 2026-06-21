package com.dusht.data.nutrition

import com.dusht.core.logging.AppLogger
import com.dusht.data.local.dao.NutritionDao
import com.dusht.data.local.entity.DayLogEntity
import com.dusht.data.local.entity.toEntity
import com.dusht.data.nutrition.dto.DayLogDto
import com.dusht.data.nutrition.dto.toDto
import com.dusht.shared.model.DayNutrition
import com.dusht.shared.model.MealEntry
import com.dusht.shared.repository.NutritionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val nutritionDao: NutritionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : NutritionRepository {

    /**
     * Singleton-scoped background scope for Firestore sync.
     * SupervisorJob ensures individual sync failures don't cancel the scope.
     */
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun uid(): String? = auth.currentUser?.uid

    private fun nutritionCol(uid: String) =
        firestore.collection("users").document(uid).collection("nutrition")

    private fun dateKey(year: Int, month: Int, day: Int) =
        "%04d-%02d-%02d".format(year, month, day)

    private fun parseDate(s: String): Triple<Int, Int, Int>? {
        val p = s.split("-")
        if (p.size != 3) return null
        return Triple(p[0].toIntOrNull() ?: return null,
            p[1].toIntOrNull() ?: return null,
            p[2].toIntOrNull() ?: return null)
    }

    // ── Public API ────────────────────────────────────────────────────────────

    override fun observeMonthLogs(year: Int, month: Int): Flow<Map<Int, DayNutrition>> {
        val uid = uid() ?: return flowOf(emptyMap())
        // Kick off background sync; UI updates automatically when Room changes.
        syncScope.launch { syncMonthFromFirestore(uid, year, month) }
        return combine(
            nutritionDao.observeMonthDayLogs(uid, year, month),
            nutritionDao.observeMonthMeals(uid, year, month),
        ) { dayLogs, meals ->
            val mealsByDay = meals.groupBy { it.day }
            dayLogs.associate { dl ->
                dl.day to DayNutrition(
                    year = dl.year, month = dl.month, day = dl.day,
                    calorieGoal = dl.calorieGoal,
                    meals = mealsByDay[dl.day]?.map { it.toDomain() } ?: emptyList(),
                )
            }
        }
    }

    override suspend fun addMeal(year: Int, month: Int, day: Int, meal: MealEntry): Result<Unit> {
        val uid = uid() ?: return Result.failure(IllegalStateException("Not authenticated"))
        // 1. Write to Room immediately → UI updates in the same frame.
        nutritionDao.insertDayLogIfNew(DayLogEntity(uid, year, month, day,
            calorieGoal = nutritionDao.getCalorieGoalForDay(uid, year, month, day) ?: 2000))
        nutritionDao.insertMeal(meal.toEntity(uid, year, month, day, pendingSync = true))
        // 2. Push the complete day to Firestore in background.
        syncScope.launch { pushDayToFirestore(uid, year, month, day) }
        return Result.success(Unit)
    }

    override suspend fun updateMeal(year: Int, month: Int, day: Int, meal: MealEntry): Result<Unit> {
        val uid = uid() ?: return Result.failure(IllegalStateException("Not authenticated"))
        nutritionDao.insertMeal(meal.toEntity(uid, year, month, day, pendingSync = true))
        syncScope.launch { pushDayToFirestore(uid, year, month, day) }
        return Result.success(Unit)
    }

    override suspend fun deleteMeal(year: Int, month: Int, day: Int, mealId: String): Result<Unit> {
        val uid = uid() ?: return Result.failure(IllegalStateException("Not authenticated"))
        // Soft-delete in Room so the UI removes it immediately.
        nutritionDao.softDeleteMeal(uid, mealId)
        // Rebuild and push the day doc (minus the deleted meal) to Firestore.
        syncScope.launch { pushDayToFirestore(uid, year, month, day) }
        return Result.success(Unit)
    }

    override suspend fun setDayCalorieGoal(year: Int, month: Int, day: Int, goal: Int): Result<Unit> {
        val uid = uid() ?: return Result.failure(IllegalStateException("Not authenticated"))
        nutritionDao.updateCalorieGoal(uid, year, month, day, goal)
        syncScope.launch { pushDayToFirestore(uid, year, month, day) }
        return Result.success(Unit)
    }

    // ── Sync helpers ──────────────────────────────────────────────────────────

    /**
     * Pushes the current Room state for a single day to Firestore.
     *
     * After a successful write it:
     *  - Marks all locally-written meals as synced (pendingSync = false).
     *  - Hard-deletes any soft-deleted meals for that day from Room.
     */
    private suspend fun pushDayToFirestore(uid: String, year: Int, month: Int, day: Int) {
        try {
            val meals = nutritionDao.getMealsForDay(uid, year, month, day)
            val goal = nutritionDao.getCalorieGoalForDay(uid, year, month, day) ?: 2000
            val key = dateKey(year, month, day)
            val dto = DayLogDto(date = key, calorieGoal = goal, meals = meals.map { it.toDto() })
            nutritionCol(uid).document(key).set(dto).await()
            // Mark written meals as synced.
            meals.forEach { nutritionDao.markMealSynced(it.id) }
            // Hard-delete soft-deleted meals for this day now that Firestore is updated.
            nutritionDao.getPendingDeletes(uid)
                .filter { it.year == year && it.month == month && it.day == day }
                .forEach { nutritionDao.hardDeleteMeal(it.id) }
        } catch (e: Exception) {
            // Offline or transient error — pendingSync stays true; will retry on next sync.
            AppLogger.api("pushDayToFirestore failed", extras = mapOf("error" to (e.message ?: "")))
        }
    }

    /**
     * Pulls all day-docs for a month from Firestore and upserts into Room.
     *
     * Conflict strategy:
     *  - Local meals with pendingSync=true → kept as-is (they're ahead of Firestore).
     *  - Local meals with pendingSync=false → replaced by the Firestore version (authoritative).
     *  - Meals in Firestore but absent locally → inserted.
     *  - Meals absent from Firestore but present locally as synced → deleted (removed on another device).
     */
    private suspend fun syncMonthFromFirestore(uid: String, year: Int, month: Int) {
        try {
            val start = "%04d-%02d-01".format(year, month)
            val end = "%04d-%02d-31".format(year, month)
            val snapshot = nutritionCol(uid)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .get().await()

            val now = System.currentTimeMillis()
            snapshot.documents.forEach { doc ->
                val dto = doc.toObject(DayLogDto::class.java) ?: return@forEach
                val (y, m, d) = parseDate(doc.id) ?: return@forEach

                // Upsert the day-level record.
                nutritionDao.upsertDayLog(DayLogEntity(uid, y, m, d, dto.calorieGoal, syncedAt = now))

                // IDs of meals the user wrote locally but hasn't pushed yet.
                val pendingIds = nutritionDao.getPendingMealsForDay(uid, y, m, d)
                    .map { it.id }.toSet()

                // Replace synced local meals with the Firestore version (handles remote deletes).
                nutritionDao.deleteSyncedMealsForDay(uid, y, m, d)

                // Insert Firestore meals, skipping any that are locally pending.
                val incoming = dto.meals
                    .filter { it.id !in pendingIds }
                    .map { it.toEntity(uid, y, m, d, pendingSync = false) }
                nutritionDao.insertMeals(incoming)
            }
        } catch (e: Exception) {
            // Offline — Room data is served as-is, no update needed.
            AppLogger.api("syncMonthFromFirestore failed", extras = mapOf("error" to (e.message ?: "")))
        }
    }
}
