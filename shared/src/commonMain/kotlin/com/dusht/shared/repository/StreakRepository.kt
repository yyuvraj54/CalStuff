package com.dusht.shared.repository

import com.dusht.shared.model.StreakData
import kotlinx.coroutines.flow.Flow

/**
 * Contract for reading and updating the user's logging streak.
 *
 * Firestore layout (per user):
 *   users/{uid}/streaks/current
 *     currentStreak: Int
 *     longestStreak: Int
 *     lastLoggedDate: String    "YYYY-MM-DD"
 *     updatedAt: Long           epoch-ms
 *
 * Implementations:
 *  - Android/Firebase: FirestoreStreakRepositoryImpl (data module)
 *  - Future custom backend: swap binding in DataModule — no ViewModel changes needed.
 */
interface StreakRepository {

    /** Live stream of the user's current streak state. */
    fun observeStreak(): Flow<StreakData>

    /**
     * Called whenever the user logs a meal for today.
     * Computes and persists the new streak, then returns it.
     * todayDate must be "YYYY-MM-DD".
     */
    suspend fun updateStreak(todayDate: String): Result<StreakData>
}
