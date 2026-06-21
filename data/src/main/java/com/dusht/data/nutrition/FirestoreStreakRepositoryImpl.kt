package com.dusht.data.nutrition

import com.dusht.data.nutrition.dto.StreakDto
import com.dusht.data.nutrition.dto.toDto
import com.dusht.shared.model.StreakData
import com.dusht.shared.repository.StreakRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreStreakRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : StreakRepository {

    private fun uid(): String? = auth.currentUser?.uid

    private fun streakDoc(uid: String) =
        firestore.collection("users").document(uid)
            .collection("streaks").document("current")

    override fun observeStreak(): Flow<StreakData> {
        val uid = uid() ?: return flowOf(StreakData())
        return callbackFlow {
            val listener = streakDoc(uid).addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend((snapshot?.toObject(StreakDto::class.java) ?: StreakDto()).toDomain())
            }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun updateStreak(todayDate: String): Result<StreakData> {
        val uid = uid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val docRef = streakDoc(uid)
        return runCatching {
            firestore.runTransaction { tx ->
                val existing = tx.get(docRef).toObject(StreakDto::class.java) ?: StreakDto()
                val newStreak = computeNewStreak(existing, todayDate)
                tx.set(docRef, newStreak.toDto())
                newStreak
            }.await()
        }
    }

    /**
     * Streak rules:
     *  - Same day logged again → no change.
     *  - Logged yesterday → increment streak.
     *  - Any gap → reset to 1 (today counts as day 1).
     */
    private fun computeNewStreak(existing: StreakDto, todayDate: String): StreakData {
        if (existing.lastLoggedDate == todayDate) return existing.toDomain()

        val newCurrent = if (isDayBefore(existing.lastLoggedDate, todayDate)) {
            existing.currentStreak + 1
        } else {
            1
        }
        return StreakData(
            currentStreak = newCurrent,
            longestStreak = maxOf(existing.longestStreak, newCurrent),
            lastLoggedDate = todayDate,
        )
    }

    /** Returns true if [earlier] is exactly one calendar day before [later] ("YYYY-MM-DD"). */
    private fun isDayBefore(earlier: String, later: String): Boolean {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val earlyTime = fmt.parse(earlier)?.time ?: return false
            val laterTime = fmt.parse(later)?.time ?: return false
            // Integer day difference avoids DST edge cases
            val diffDays = ((laterTime - earlyTime) / (1_000L * 60 * 60 * 24)).toInt()
            diffDays == 1
        } catch (e: Exception) {
            false
        }
    }
}
