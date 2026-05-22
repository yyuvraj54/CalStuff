package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Calendar

private const val COLUMNS = 7

@Composable
fun MonthCalendarGrid(
    year: Int,
    month: Int, // 0-based (Calendar.JANUARY = 0)
    highlightedDays: Set<Int>,
    modifier: Modifier = Modifier
) {
    val firstDayOfWeek = remember(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        // Calendar.DAY_OF_WEEK: Sunday=1 .. Saturday=7
        // Convert to Monday=0 .. Sunday=6
        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }
    val totalDays = remember(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val today = remember {
        val cal = Calendar.getInstance()
        cal.get(Calendar.DAY_OF_MONTH)
    }
    val isCurrentMonth = remember(year, month) {
        val cal = Calendar.getInstance()
        year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH)
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val spacing = 6.dp
        val availableWidth = maxWidth - spacing * (COLUMNS - 1)
        val circleSize = availableWidth / COLUMNS

        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            var dayCounter = 1

            // First row: offset by firstDayOfWeek empty slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                repeat(firstDayOfWeek) {
                    Box(modifier = Modifier.weight(1f))
                }
                val daysInFirstRow = COLUMNS - firstDayOfWeek
                repeat(daysInFirstRow) {
                    if (dayCounter <= totalDays) {
                        DayCircle(
                            day = dayCounter,
                            isHighlighted = dayCounter in highlightedDays,
                            isToday = isCurrentMonth && dayCounter == today,
                            size = circleSize,
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    }
                }
            }

            // Remaining rows
            while (dayCounter <= totalDays) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    repeat(COLUMNS) {
                        if (dayCounter <= totalDays) {
                            DayCircle(
                                day = dayCounter,
                                isHighlighted = dayCounter in highlightedDays,
                                isToday = isCurrentMonth && dayCounter == today,
                                size = circleSize,
                                modifier = Modifier.weight(1f)
                            )
                            dayCounter++
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, widthDp = 320)
@Composable
private fun PreviewMonthCalendarGrid() {
    val cal = Calendar.getInstance()
    MonthCalendarGrid(
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH),
        highlightedDays = setOf(1, 3, 5, 7, 10, 12, 15, 17),
        modifier = Modifier.padding(12.dp)
    )
}
