package com.dusht.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dusht.shared.model.StreakData

@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey val uid: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastLoggedDate: String,
    val updatedAt: Long,
    val syncedAt: Long = 0L,
) {
    fun toDomain() = StreakData(
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastLoggedDate = lastLoggedDate,
    )
}

fun StreakData.toEntity(uid: String, syncedAt: Long = 0L) = StreakEntity(
    uid = uid,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastLoggedDate = lastLoggedDate,
    updatedAt = System.currentTimeMillis(),
    syncedAt = syncedAt,
)
