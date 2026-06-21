package com.dusht.data.nutrition

import com.dusht.data.local.dao.StreakDao
import com.dusht.data.local.entity.StreakEntity
import com.dusht.data.local.entity.toEntity
import com.dusht.data.nutrition.dto.StreakDto
import com.dusht.data.nutrition.dto.toDto
import com.dusht.shared.model.StreakData
import com.dusht.shared.repository.StreakRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepositoryImpl @Inject constructor(
    private val streakDao: StreakDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : StreakRepository {

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun uid(): String? = auth.currentUser?.uid

    private fun streakDoc(uid: String) =
        firestore.collection("users").document(uid).collection("streaks").document("current")

    override fun observeStreak(): Flow<StreakData> {
        val uid = uid() ?: return flowOf(StreakData())
        syncScope.launch { syncStreakFromFirestore(uid) }
        return streakDao.observeStreak(uid).map { it?.toDomain() ?: StreakData() }
    }

    override suspend fun updateStreak(todayDate: String): Result<StreakData> {
        val uid = uid() ?: return Result.failure(IllegalStateException("Not authenticated"))

        val existing = streakDao.getStreak(uid)
            ?: StreakEntity(uid, 0, 0, "", System.currentTimeMillis())

        if (existing.lastLoggedDate == todayDate) return Result.success(existing.toDomain())

        val newCurrent = if (isDayBefore(existing.lastLoggedDate, todayDate)) {
            existing.currentStreak + 1
        } else {
            1
        }
        val newStreak = StreakData(
            currentStreak = newCurrent,
            longestStreak = maxOf(existing.longestStreak, newCurrent),
            lastLoggedDate = todayDate,
        )

        // Write to Room immediately.
        streakDao.upsertStreak(newStreak.toEntity(uid))

        // Push to Firestore in background.
        syncScope.launch {
            runCatching { streakDoc(uid).set(newStreak.toDto()).await() }
        }

        return Result.success(newStreak)
    }

    private suspend fun syncStreakFromFirestore(uid: String) {
        if (streakDao.getStreak(uid) != null) return
        runCatching {
            val dto = streakDoc(uid).get().await().toObject(StreakDto::class.java) ?: return
            streakDao.upsertStreak(dto.toDomain().toEntity(uid, syncedAt = System.currentTimeMillis()))
        }
    }

    private fun isDayBefore(earlier: String, later: String): Boolean {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val e = fmt.parse(earlier)?.time ?: return false
            val l = fmt.parse(later)?.time ?: return false
            ((l - e) / (1_000L * 60 * 60 * 24)).toInt() == 1
        } catch (e: Exception) {
            false
        }
    }
}
