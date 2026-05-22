package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private const val GRID_COLUMNS = 7

@Composable
fun CalendarProgressCardCompact(
    percentage: Int,
    year: Int,
    month: Int,
    highlightedDays: Set<Int>,
    modifier: Modifier = Modifier
) {
    val firstDayOfWeek = remember(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }
    val totalDays = remember(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val today = remember {
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }
    val isCurrentMonth = remember(year, month) {
        val cal = Calendar.getInstance()
        year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH)
    }
    val monthName = remember(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, java.util.Locale.getDefault()) ?: ""
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 20.dp)
        ) {
            val spacing = 6.dp
            val availableWidth = maxWidth - spacing * (GRID_COLUMNS - 1)
            val circleSize = availableWidth / GRID_COLUMNS

            val percentageRowSpan = 2
            val percentageColSpan = 2

            Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                var dayCounter = 1

                // Top section: percentage on left, circles on right
                Row {
                    // Left: percentage + month
                    Column(
                        modifier = Modifier
                            .width((circleSize * percentageColSpan) + (spacing * (percentageColSpan - 1)))
                            .height((circleSize * percentageRowSpan) + (spacing * (percentageRowSpan - 1))),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$percentage%",
                            color = AppYellow,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp,
                            maxLines = 1
                        )
                        Text(
                            text = monthName,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(spacing))

                    // Right: circles filling remaining columns across 2 rows
                    val rightCols = GRID_COLUMNS - percentageColSpan
                    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                        repeat(percentageRowSpan) { rowIndex ->
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                                repeat(rightCols) {
                                    val gridCol = percentageColSpan + it
                                    val cellIndex = rowIndex * GRID_COLUMNS + gridCol

                                    if (cellIndex >= firstDayOfWeek && dayCounter <= totalDays) {
                                        DayCircle(
                                            day = dayCounter,
                                            isHighlighted = dayCounter in highlightedDays,
                                            isToday = isCurrentMonth && dayCounter == today,
                                            size = circleSize
                                        )
                                        dayCounter++
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .width(circleSize)
                                                .height(circleSize)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Remaining full-width rows
                while (dayCounter <= totalDays) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        repeat(GRID_COLUMNS) {
                            if (dayCounter <= totalDays) {
                                DayCircle(
                                    day = dayCounter,
                                    isHighlighted = dayCounter in highlightedDays,
                                    isToday = isCurrentMonth && dayCounter == today,
                                    size = circleSize
                                )
                                dayCounter++
                            } else {
                                Box(
                                    modifier = Modifier
                                        .width(circleSize)
                                        .height(circleSize)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFECF4F)
@Composable
private fun PreviewCalendarProgressCardCompact() {
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_MONTH)
    CalendarProgressCardCompact(
        percentage = (today * 100) / cal.getActualMaximum(Calendar.DAY_OF_MONTH),
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH),
        highlightedDays = (1..today).toSet(),
        modifier = Modifier.padding(16.dp)
    )
}
