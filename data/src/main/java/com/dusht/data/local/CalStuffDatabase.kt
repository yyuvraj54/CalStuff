package com.dusht.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dusht.data.local.dao.NutritionDao
import com.dusht.data.local.dao.StreakDao
import com.dusht.data.local.dao.UserProfileDao
import com.dusht.data.local.entity.DayLogEntity
import com.dusht.data.local.entity.MealEntryEntity
import com.dusht.data.local.entity.StreakEntity
import com.dusht.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        DayLogEntity::class,
        MealEntryEntity::class,
        StreakEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class CalStuffDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun streakDao(): StreakDao
}
