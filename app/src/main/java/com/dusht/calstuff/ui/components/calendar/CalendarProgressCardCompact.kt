package com.dusht.calstuff.ui.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dusht.calstuff.ui.theme.FontSize
import java.util.Calendar

private const val GRID_COLUMNS = 7
private val DAY_HEADERS = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun CalendarProgressCardCompact(
    percentage: Int,
    year: Int,
    month: Int,
    highlightedDays: Set<Int>,
    caloriesConsumed: Int = 0,
    calorieGoal: Int = 0,
    dayCalorieRatios: Map<Int, Float> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val firstDayOfWeek = remember(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        // Monday=0 .. Sunday=6
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            val spacing = 4.dp
            val availableWidth = maxWidth - spacing * (GRID_COLUMNS - 1)
            val circleSize = availableWidth / GRID_COLUMNS

            Column {
                // Header: percentage + month
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$percentage%",
                        color = AppYellow,
                        fontSize = FontSize.xLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = monthName,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = FontSize.xSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day headers row
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
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = FontSize.xxxSmall,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Calendar grid — all rows are full 7-column
                Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                    var dayCounter = 1

                    // First row with offset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        repeat(firstDayOfWeek) {
                            Box(modifier = Modifier.weight(1f))
                        }
                        repeat(GRID_COLUMNS - firstDayOfWeek) {
                            if (dayCounter <= totalDays) {
                                DayCircle(
                                    day = dayCounter,
                                    isHighlighted = dayCounter in highlightedDays,
                                    isToday = isCurrentMonth && dayCounter == today,
                                    calorieRatio = dayCalorieRatios[dayCounter],
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
                            repeat(GRID_COLUMNS) {
                                if (dayCounter <= totalDays) {
                                    val d = dayCounter
                                    DayCircle(
                                        day = d,
                                        isHighlighted = d in highlightedDays,
                                        isToday = isCurrentMonth && d == today,
                                        calorieRatio = dayCalorieRatios[d],
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

                // Summary text
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (calorieGoal > 0) "$caloriesConsumed / $calorieGoal kcal"
                        else "$percentage% of $monthName",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = FontSize.xxxSmall,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 200)
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

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 200)
@Composable
private fun PreviewCalendarFeb() {
    CalendarProgressCardCompact(
        percentage = 50,
        year = 2026,
        month = Calendar.FEBRUARY,
        highlightedDays = (1..14).toSet(),
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 200)
@Composable
private fun PreviewCalendarJan() {
    CalendarProgressCardCompact(
        percentage = 100,
        year = 2026,
        month = Calendar.JANUARY,
        highlightedDays = (1..31).toSet(),
        modifier = Modifier.padding(16.dp)
    )
}
