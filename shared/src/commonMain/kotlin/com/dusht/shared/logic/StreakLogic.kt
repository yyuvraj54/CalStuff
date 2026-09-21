package com.dusht.shared.logic

import com.dusht.shared.model.StreakData

/**
 * Pure streak increment/reset math, shared so Android and iOS always compute the same streak
 * from the same (existing, todayDate) pair — no platform date APIs involved.
 */
object StreakLogic {

    /**
     * Returns the [StreakData] after logging on [todayDate] ("YYYY-MM-DD").
     * No-op if already logged today; +1 if logged exactly one calendar day after
     * [StreakData.lastLoggedDate]; resets to 1 otherwise (gap, or no prior date).
     */
    fun next(existing: StreakData, todayDate: String): StreakData {
        if (existing.lastLoggedDate == todayDate) return existing
        val newCurrent = if (isExactlyOneDayAfter(existing.lastLoggedDate, todayDate)) {
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

    private fun isExactlyOneDayAfter(earlier: String, later: String): Boolean {
        val e = parseEpochDay(earlier) ?: return false
        val l = parseEpochDay(later) ?: return false
        return l - e == 1L
    }

    private fun parseEpochDay(dateStr: String): Long? {
        val parts = dateStr.split("-")
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        return daysFromCivil(y, m, d)
    }

    /**
     * Howard Hinnant's `days_from_civil` (public domain) — proleptic-Gregorian day count from
     * 1970-01-01, in pure integer math. Avoids pulling in a date/time library for one calculation.
     */
    private fun daysFromCivil(y: Int, m: Int, d: Int): Long {
        val yAdj = if (m <= 2) y - 1 else y
        val era = (if (yAdj >= 0) yAdj else yAdj - 399) / 400
        val yoe = yAdj - era * 400
        val doy = (153 * (if (m > 2) m - 3 else m + 9) + 2) / 5 + d - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097L + doe - 719468L
    }
}
