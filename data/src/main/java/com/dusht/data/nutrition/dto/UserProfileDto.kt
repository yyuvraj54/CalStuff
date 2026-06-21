package com.dusht.data.nutrition.dto

import com.dusht.shared.model.UserProfile
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Firestore POJO for users/{uid}.
 * All fields have defaults so Firestore can deserialise via no-arg constructor.
 * @DocumentId populates uid from the Firestore document ID automatically.
 */
@IgnoreExtraProperties
data class UserProfileDto(
    @DocumentId val uid: String = "",
    val name: String = "",
    val age: Int = 0,
    val heightCm: Float = 0f,
    val weightKg: Float = 0f,
    val gender: String = "",
    val activityLevel: String = "",
    val dailyCalorieGoal: Int = 2000,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    fun toDomain() = UserProfile(
        uid = uid,
        name = name,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        gender = gender,
        activityLevel = activityLevel,
        dailyCalorieGoal = dailyCalorieGoal,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun UserProfile.toDto() = UserProfileDto(
    uid = uid,
    name = name,
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    gender = gender,
    activityLevel = activityLevel,
    dailyCalorieGoal = dailyCalorieGoal,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
