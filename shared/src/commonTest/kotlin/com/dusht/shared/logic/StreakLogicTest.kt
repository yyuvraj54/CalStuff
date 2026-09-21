package com.dusht.shared.logic

import com.dusht.shared.model.StreakData
import kotlin.test.Test
import kotlin.test.assertEquals

class StreakLogicTest {

    @Test
    fun firstEverLog_startsStreakAtOne() {
        val result = StreakLogic.next(StreakData(), "2026-07-09")
        assertEquals(StreakData(currentStreak = 1, longestStreak = 1, lastLoggedDate = "2026-07-09"), result)
    }

    @Test
    fun consecutiveDay_incrementsCurrentAndLongest() {
        val existing = StreakData(currentStreak = 3, longestStreak = 5, lastLoggedDate = "2026-07-08")
        val result = StreakLogic.next(existing, "2026-07-09")
        assertEquals(StreakData(currentStreak = 4, longestStreak = 5, lastLoggedDate = "2026-07-09"), result)
    }

    @Test
    fun consecutiveDay_newRecord_updatesLongest() {
        val existing = StreakData(currentStreak = 5, longestStreak = 5, lastLoggedDate = "2026-07-08")
        val result = StreakLogic.next(existing, "2026-07-09")
        assertEquals(StreakData(currentStreak = 6, longestStreak = 6, lastLoggedDate = "2026-07-09"), result)
    }

    @Test
    fun sameDayTwice_isNoOp() {
        val existing = StreakData(currentStreak = 4, longestStreak = 9, lastLoggedDate = "2026-07-09")
        val result = StreakLogic.next(existing, "2026-07-09")
        assertEquals(existing, result)
    }

    @Test
    fun gapOfTwoDays_resetsToOne() {
        val existing = StreakData(currentStreak = 10, longestStreak = 10, lastLoggedDate = "2026-07-01")
        val result = StreakLogic.next(existing, "2026-07-09")
        assertEquals(StreakData(currentStreak = 1, longestStreak = 10, lastLoggedDate = "2026-07-09"), result)
    }

    @Test
    fun crossesMonthBoundary_stillCountsAsConsecutive() {
        val existing = StreakData(currentStreak = 1, longestStreak = 1, lastLoggedDate = "2026-06-30")
        val result = StreakLogic.next(existing, "2026-07-01")
        assertEquals(StreakData(currentStreak = 2, longestStreak = 2, lastLoggedDate = "2026-07-01"), result)
    }

    @Test
    fun crossesYearBoundary_stillCountsAsConsecutive() {
        val existing = StreakData(currentStreak = 1, longestStreak = 1, lastLoggedDate = "2025-12-31")
        val result = StreakLogic.next(existing, "2026-01-01")
        assertEquals(StreakData(currentStreak = 2, longestStreak = 2, lastLoggedDate = "2026-01-01"), result)
    }

    @Test
    fun leapDayBoundary_stillCountsAsConsecutive() {
        val existing = StreakData(currentStreak = 1, longestStreak = 1, lastLoggedDate = "2024-02-28")
        val result = StreakLogic.next(existing, "2024-02-29")
        assertEquals(StreakData(currentStreak = 2, longestStreak = 2, lastLoggedDate = "2024-02-29")
            , result)
    }

    @Test
    fun emptyLastLoggedDate_startsFreshStreak() {
        val existing = StreakData(currentStreak = 0, longestStreak = 0, lastLoggedDate = "")
        val result = StreakLogic.next(existing, "2026-07-09")
        assertEquals(StreakData(currentStreak = 1, longestStreak = 1, lastLoggedDate = "2026-07-09"), result)
    }

    @Test
    fun goingBackwardsInTime_resetsToOne() {
        // Defensive: a clock skew or bad input shouldn't silently keep incrementing.
        val existing = StreakData(currentStreak = 5, longestStreak = 5, lastLoggedDate = "2026-07-09")
        val result = StreakLogic.next(existing, "2026-07-05")
        assertEquals(StreakData(currentStreak = 1, longestStreak = 5, lastLoggedDate = "2026-07-05"), result)
    }
}
