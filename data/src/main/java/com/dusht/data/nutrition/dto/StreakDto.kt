package com.dusht.data.nutrition.dto

import com.dusht.shared.model.StreakData
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Firestore POJO for users/{uid}/streaks/current.
 */
@IgnoreExtraProperties
data class StreakDto(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLoggedDate: String = "",
    val updatedAt: Long = 0L,
) {
    fun toDomain() = StreakData(
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastLoggedDate = lastLoggedDate,
    )
}

fun StreakData.toDto(updatedAt: Long = System.currentTimeMillis()) = StreakDto(
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastLoggedDate = lastLoggedDate,
    updatedAt = updatedAt,
)
