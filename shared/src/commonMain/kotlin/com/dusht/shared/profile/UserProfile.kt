package com.dusht.shared.profile

/**
 * Onboarding / profile fields persisted per Firebase Auth user (e.g. Firestore `users/{uid}`).
 * Gender and activity are stored as enum names matching the Android UI enums.
 */
data class UserProfile(
    val name: String = "",
    val age: Int? = null,
    val gender: String? = null,
    val heightCm: Int? = null,
    val weightKg: Int? = null,
    val activity: String? = null,
) {
    fun isComplete(): Boolean =
        name.trim().length >= 2 &&
            age != null && age in 13..120 &&
            !gender.isNullOrBlank() &&
            heightCm != null && heightCm in 100..250 &&
            weightKg != null && weightKg in 35..300 &&
            !activity.isNullOrBlank()
}
