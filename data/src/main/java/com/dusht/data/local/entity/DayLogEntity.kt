package com.dusht.data.local.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * One row per calendar day per user.
 * Composite PK: uid + year + month + day.
 */
@Entity(
    tableName = "day_log",
    primaryKeys = ["uid", "year", "month", "day"],
    indices = [Index(value = ["uid", "year", "month"])],
)
data class DayLogEntity(
    val uid: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val calorieGoal: Int,
    /** Epoch-ms of the last successful Firestore pull for this day. */
    val syncedAt: Long = 0L,
)
