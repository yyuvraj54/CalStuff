package com.dusht.calstuff.ui.model

/** Presentation color for each type is resolved from the design system — see `MealType.themeColor()`. */
enum class MealType(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACKS("Snacks / Misc")
}
