package com.dusht.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dusht.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Query("SELECT * FROM streak WHERE uid = :uid")
    fun observeStreak(uid: String): Flow<StreakEntity?>

    @Query("SELECT * FROM streak WHERE uid = :uid")
    suspend fun getStreak(uid: String): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: StreakEntity)
}
