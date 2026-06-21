package com.dusht.shared.model

/**
 * Canonical user profile domain model.
 * Lives in shared so iOS can use it when the KMP data layer is wired.
 * gender/activityLevel stored as strings ("MALE", "SEDENTARY", etc.) to stay
 * serialisation-agnostic — callers convert to their own enums.
 */
data class UserProfile(
    val uid: String,
    val name: String,
    val age: Int,
    val heightCm: Float,
    val weightKg: Float,
    /** "MALE" | "FEMALE" | "OTHER" | "PREFER_NOT_SAY" */
    val gender: String,
    /** "SEDENTARY" | "LIGHT" | "MODERATE" | "ACTIVE" | "VERY_ACTIVE" */
    val activityLevel: String,
    val dailyCalorieGoal: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
