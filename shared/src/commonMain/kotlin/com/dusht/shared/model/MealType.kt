package com.dusht.shared.model

/**
 * Canonical meal-type values. [wireValue] is what's stored on [MealEntry.mealType] and in
 * Firestore — keep it stable, both platforms parse against it.
 */
enum class MealType(val wireValue: String) {
    BREAKFAST("BREAKFAST"),
    LUNCH("LUNCH"),
    DINNER("DINNER"),
    SNACKS("SNACKS"),
}
