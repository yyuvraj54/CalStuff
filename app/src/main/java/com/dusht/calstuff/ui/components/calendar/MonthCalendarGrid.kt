package com.dusht.calstuff.ui.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dusht.calstuff.ui.theme.FontSize
import com.dusht.calstuff.ui.theme.calStuffColors
import java.util.Calendar

private const val COLUMNS = 7

/** Fixed row count so the grid's height is constant across months (max any month ever needs is 6). */
private const val TOTAL_ROWS = 6
private val DAY_HEADERS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun MonthCalendarGrid(
    year: Int,
    month: Int, // 0-based (Calendar.JANUARY = 0)
    highlightedDays: Set<Int>,
    dayCalorieRatios: Map<Int, Float> = emptyMap(),
    selectedDay: Int? = null,
    onDayClick: ((Int) -> Unit)? = null,
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
    val colors = MaterialTheme.calStuffColors

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val spacing = 6.dp
        val availableWidth = maxWidth - spacing * (COLUMNS - 1)
        val circleSize = availableWidth / COLUMNS

        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            // Day name headers (Mon - Sun)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                DAY_HEADERS.forEach { name ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            color = colors.onInverseSurface.copy(alpha = 0.4f),
                            fontSize = FontSize.xxxSmall,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Date circles.
            // Always render a fixed 6 week-rows (the max any month ever needs) so the grid's
            // height stays constant across months instead of growing/shrinking by 1-2 rows.
            var dayCounter = 1

            // First row: offset by firstDayOfWeek empty slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                repeat(firstDayOfWeek) {
                    Box(modifier = Modifier.weight(1f).height(circleSize))
                }
                val daysInFirstRow = COLUMNS - firstDayOfWeek
                repeat(daysInFirstRow) {
                    if (dayCounter <= totalDays) {
                        val d = dayCounter
                        DayCircle(
                            day = d,
                            isHighlighted = d in highlightedDays,
                            isToday = isCurrentMonth && d == today,
                            isSelected = d == selectedDay,
                            calorieRatio = dayCalorieRatios[d],
                            size = circleSize,
                            onClick = onDayClick?.let { { it(d) } },
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    } else {
                        Box(modifier = Modifier.weight(1f).height(circleSize))
                    }
                }
            }

            // Remaining 5 rows — always rendered, filled with real days or blank cells.
            repeat(TOTAL_ROWS - 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    repeat(COLUMNS) {
                        if (dayCounter <= totalDays) {
                            val d = dayCounter
                            DayCircle(
                                day = d,
                                isHighlighted = d in highlightedDays,
                                isToday = isCurrentMonth && d == today,
                                isSelected = d == selectedDay,
                                calorieRatio = dayCalorieRatios[d],
                                size = circleSize,
                                onClick = onDayClick?.let { { it(d) } },
                                modifier = Modifier.weight(1f)
                            )
                            dayCounter++
                        } else {
                            Box(modifier = Modifier.weight(1f).height(circleSize))
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
