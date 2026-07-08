package com.dusht.calstuff.ui.theme

import androidx.compose.ui.graphics.Color
import com.dusht.calstuff.ui.model.MealType

/**
 * Themed dot/tag color for a meal type — consistent everywhere a meal type is shown.
 * All four are deliberately theme-stable (not [CalStuffColors.textPrimary]) since some call
 * sites (e.g. the meal-type selector pill) use this as a filled chip background and pair it
 * with a fixed contrasting text color — that pairing would break if this flipped per theme.
 */
fun MealType.themeColor(colors: CalStuffColors): Color = when (this) {
    MealType.BREAKFAST -> colors.highlight
    MealType.LUNCH -> colors.error
    MealType.DINNER -> colors.inverseSurface
    MealType.SNACKS -> colors.success
}
