package com.dusht.shared.logic

/**
 * Canonical "YYYY-MM-DD" key used as the Firestore document ID for `users/{uid}/nutrition/{key}`.
 * Both platforms must build/parse this identically since they query against it directly
 * (via document-ID range queries, not a stored field — see [NutritionRepository] docs).
 */
object DateKey {
    fun of(year: Int, month: Int, day: Int): String {
        val y = year.toString().padStart(4, '0')
        val m = month.toString().padStart(2, '0')
        val d = day.toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    /** Parses a "YYYY-MM-DD" key back into (year, month, day), or null if malformed. */
    fun parse(key: String): Triple<Int, Int, Int>? {
        val parts = key.split("-")
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        return Triple(y, m, d)
    }
}
