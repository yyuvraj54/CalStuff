package com.dusht.calstuff.ui.model.widget

import kotlinx.serialization.Serializable

@Serializable
data class WidgetPlacement(
    val id: String,
    val typeName: String, // WidgetType.name for serialization
    val col: Int,
    val row: Int,
    val spanX: Int,
    val spanY: Int
) {
    val type: WidgetType
        get() = WidgetType.valueOf(typeName)

    companion object {
        fun create(type: WidgetType, col: Int = 0, row: Int = 0): WidgetPlacement {
            return WidgetPlacement(
                id = "${type.name}_${System.nanoTime()}",
                typeName = type.name,
                col = col,
                row = row,
                spanX = type.spanX,
                spanY = type.spanY
            )
        }
    }
}

@Serializable
data class GridLayout(
    val columns: Int = 4,
    val placements: List<WidgetPlacement> = defaultPlacements()
)

fun defaultPlacements(): List<WidgetPlacement> = listOf(
    WidgetPlacement.create(WidgetType.STREAK, col = 0, row = 0),
    WidgetPlacement.create(WidgetType.CALENDAR_COMPACT, col = 2, row = 0),
    WidgetPlacement.create(WidgetType.DAILY_PROGRESS, col = 0, row = 2),
    WidgetPlacement.create(WidgetType.WEEKLY_CHART, col = 0, row = 5),
    WidgetPlacement.create(WidgetType.BMI, col = 0, row = 7)
)
