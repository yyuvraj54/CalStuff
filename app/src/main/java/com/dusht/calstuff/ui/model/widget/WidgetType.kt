package com.dusht.calstuff.ui.model.widget

/**
 * All available widget types with their default grid sizes.
 * Grid is 4 columns. spanX = columns, spanY = rows.
 */
enum class WidgetType(
    val displayName: String,
    val spanX: Int,
    val spanY: Int
) {
    STREAK("Streak", 2, 2),
    CALENDAR_COMPACT("Calendar", 2, 2),
    DAILY_PROGRESS("Daily Nutrition", 4, 3),
    WEEKLY_CHART("Weekly Trend", 4, 2),
    BMI("BMI", 4, 3)
}
