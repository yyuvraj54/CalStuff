package com.dusht.data.nutrition

import com.dusht.data.nutrition.dto.DayLogDto
import com.dusht.data.nutrition.dto.toDto
import com.dusht.shared.model.DayNutrition
import com.dusht.shared.model.MealEntry
import com.dusht.shared.repository.NutritionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreNutritionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : NutritionRepository {

    private fun uid(): String? = auth.currentUser?.uid

    private fun nutritionCollection(uid: String) =
        firestore.collection("users").document(uid).collection("nutrition")

    /** "2026-06-13" format used as the Firestore document ID for each day. */
    private fun dateKey(year: Int, month: Int, day: Int) =
        "%04d-%02d-%02d".format(year, month, day)

    /** Splits "YYYY-MM-DD" back into (year, month, day). Returns null on bad input. */
    private fun parseDate(dateStr: String): Triple<Int, Int, Int>? {
        val parts = dateStr.split("-")
        if (parts.size != 3) return null
        return Triple(
            parts[0].toIntOrNull() ?: return null,
            parts[1].toIntOrNull() ?: return null,
            parts[2].toIntOrNull() ?: return null,
        )
    }

    override fun observeMonthLogs(year: Int, month: Int): Flow<Map<Int, DayNutrition>> {
        val uid = uid() ?: return flowOf(emptyMap())
        val startDate = "%04d-%02d-01".format(year, month)
        val endDate = "%04d-%02d-31".format(year, month)
        return callbackFlow {
            val listener = nutritionCollection(uid)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val result = snapshot?.documents?.mapNotNull { doc ->
                        val dto = doc.toObject(DayLogDto::class.java) ?: return@mapNotNull null
                        val (y, m, d) = parseDate(doc.id) ?: return@mapNotNull null
                        dto.toDomain(y, m, d)
                    }?.associateBy { it.day } ?: emptyMap()
                    trySend(result)
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun addMeal(year: Int, month: Int, day: Int, meal: MealEntry): Result<Unit> {
        val uid = uid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val key = dateKey(year, month, day)
        return runCatching {
            val docRef = nutritionCollection(uid).document(key)
            firestore.runTransaction { tx ->
                val existing = tx.get(docRef).toObject(DayLogDto::class.java)
                val updated = DayLogDto(
                    date = key,
                    calorieGoal = existing?.calorieGoal ?: 2000,
                    meals = (existing?.meals ?: emptyList()) + meal.toDto(),
                )
                tx.set(docRef, updated)
            }.await()
        }
    }

    override suspend fun updateMeal(year: Int, month: Int, day: Int, meal: MealEntry): Result<Unit> {
        val uid = uid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val key = dateKey(year, month, day)
        return runCatching {
            val docRef = nutritionCollection(uid).document(key)
            firestore.runTransaction { tx ->
                val existing = tx.get(docRef).toObject(DayLogDto::class.java)
                    ?: return@runTransaction
                tx.set(docRef, existing.copy(
                    meals = existing.meals.map { if (it.id == meal.id) meal.toDto() else it }
                ))
            }.await()
        }
    }

    override suspend fun deleteMeal(year: Int, month: Int, day: Int, mealId: String): Result<Unit> {
        val uid = uid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val key = dateKey(year, month, day)
        return runCatching {
            val docRef = nutritionCollection(uid).document(key)
            firestore.runTransaction { tx ->
                val existing = tx.get(docRef).toObject(DayLogDto::class.java)
                    ?: return@runTransaction
                tx.set(docRef, existing.copy(
                    meals = existing.meals.filter { it.id != mealId }
                ))
            }.await()
        }
    }

    override suspend fun setDayCalorieGoal(year: Int, month: Int, day: Int, goal: Int): Result<Unit> {
        val uid = uid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val key = dateKey(year, month, day)
        return runCatching {
            nutritionCollection(uid).document(key)
                .set(mapOf("calorieGoal" to goal, "date" to key), SetOptions.merge())
                .await()
        }
    }
}
