package com.dusht.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dusht.data.nutrition.dto.MealEntryDto
import com.dusht.shared.model.MealEntry

/**
 * One row per meal.
 *
 * pendingSync = true  → written locally, not yet pushed to Firestore.
 * isDeleted   = true  → soft-deleted; will be removed from Firestore on next push then hard-deleted.
 */
@Entity(
    tableName = "meal_entry",
    indices = [
        Index(value = ["uid", "year", "month", "day"]),
        Index(value = ["pendingSync"]),
    ],
)
data class MealEntryEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val name: String,
    val mealType: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val time: String,
    val updatedAt: Long,
    val pendingSync: Boolean = true,
    val isDeleted: Boolean = false,
) {
    fun toDomain() = MealEntry(
        id = id, name = name, mealType = mealType,
        calories = calories, protein = protein, carbs = carbs,
        fat = fat, time = time,
    )

    fun toDto() = MealEntryDto(
        id = id, name = name, mealType = mealType,
        calories = calories, protein = protein, carbs = carbs,
        fat = fat, time = time,
    )
}

fun MealEntry.toEntity(
    uid: String, year: Int, month: Int, day: Int,
    pendingSync: Boolean = true,
) = MealEntryEntity(
    id = id, uid = uid, year = year, month = month, day = day,
    name = name, mealType = mealType, calories = calories,
    protein = protein, carbs = carbs, fat = fat, time = time,
    updatedAt = System.currentTimeMillis(),
    pendingSync = pendingSync,
)

fun MealEntryDto.toEntity(
    uid: String, year: Int, month: Int, day: Int,
    pendingSync: Boolean = false,
) = MealEntryEntity(
    id = id, uid = uid, year = year, month = month, day = day,
    name = name, mealType = mealType, calories = calories,
    protein = protein, carbs = carbs, fat = fat, time = time,
    updatedAt = 0L, pendingSync = pendingSync,
)
