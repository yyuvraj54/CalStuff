package com.dusht.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dusht.shared.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val age: Int,
    val heightCm: Float,
    val weightKg: Float,
    val gender: String,
    val activityLevel: String,
    val dailyCalorieGoal: Int,
    val createdAt: Long,
    val updatedAt: Long,
    /** Epoch-ms of the last Firestore pull. 0 = never synced. */
    val syncedAt: Long = 0L,
) {
    fun toDomain() = UserProfile(
        uid = uid, name = name, age = age,
        heightCm = heightCm, weightKg = weightKg,
        gender = gender, activityLevel = activityLevel,
        dailyCalorieGoal = dailyCalorieGoal,
        createdAt = createdAt, updatedAt = updatedAt,
    )
}

fun UserProfile.toEntity(syncedAt: Long = 0L) = UserProfileEntity(
    uid = uid, name = name, age = age,
    heightCm = heightCm, weightKg = weightKg,
    gender = gender, activityLevel = activityLevel,
    dailyCalorieGoal = dailyCalorieGoal,
    createdAt = createdAt, updatedAt = updatedAt,
    syncedAt = syncedAt,
)
