package com.dusht.shared.model

/**
 * Streak state for a user.
 * lastLoggedDate is "YYYY-MM-DD" (ISO-8601 date string) so it stays timezone-safe.
 */
data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    /** ISO date of the last day the user logged at least one meal. "YYYY-MM-DD" */
    val lastLoggedDate: String = "",
)
