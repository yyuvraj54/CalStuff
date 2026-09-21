package com.dusht.shared.model

/**
 * Canonical activity-level values, each carrying its Harris/Mifflin–St Jeor TDEE multiplier
 * so [com.dusht.shared.logic.CalorieGoalCalculator] has a single source for both.
 * [wireValue] is what's stored on [UserProfile.activityLevel] — keep it stable.
 */
enum class ActivityLevel(val wireValue: String, val metabolicMultiplier: Float) {
    SEDENTARY("SEDENTARY", 1.2f),
    LIGHT("LIGHT", 1.375f),
    MODERATE("MODERATE", 1.55f),
    ACTIVE("ACTIVE", 1.725f),
    VERY_ACTIVE("VERY_ACTIVE", 1.9f),
}
