package com.dusht.calstuff.ui.model.widget

/**
 * Greedy top-left bin-packing algorithm.
 * Places widgets in order, scanning row-by-row, column-by-column.
 * Guarantees no overlap and fills gaps automatically.
 */
object GridPacker {

    fun pack(widgets: List<WidgetPlacement>, columns: Int = 4): List<WidgetPlacement> {
        if (widgets.isEmpty()) return emptyList()

        // Estimate max rows needed
        val totalCells = widgets.sumOf { it.spanX * it.spanY }
        val maxRows = (totalCells / columns + widgets.size) * 2 // generous upper bound

        // Grid occupancy map
        val grid = Array(maxRows) { BooleanArray(columns) }
        val result = mutableListOf<WidgetPlacement>()

        for (widget in widgets) {
            val pos = findPosition(grid, columns, maxRows, widget.spanX, widget.spanY)
            if (pos != null) {
                val (row, col) = pos
                // Mark cells occupied
                for (r in row until row + widget.spanY) {
                    for (c in col until col + widget.spanX) {
                        grid[r][c] = true
                    }
                }
                result.add(widget.copy(col = col, row = row))
            }
            // If no position found (shouldn't happen with generous maxRows), skip widget
        }

        return result
    }

    private fun findPosition(
        grid: Array<BooleanArray>,
        columns: Int,
        maxRows: Int,
        spanX: Int,
        spanY: Int
    ): Pair<Int, Int>? {
        for (row in 0 until maxRows - spanY + 1) {
            for (col in 0..columns - spanX) {
                if (fits(grid, row, col, spanX, spanY, columns, maxRows)) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }

    private fun fits(
        grid: Array<BooleanArray>,
        row: Int,
        col: Int,
        spanX: Int,
        spanY: Int,
        columns: Int,
        maxRows: Int
    ): Boolean {
        if (col + spanX > columns || row + spanY > maxRows) return false
        for (r in row until row + spanY) {
            for (c in col until col + spanX) {
                if (grid[r][c]) return false
            }
        }
        return true
    }
}
