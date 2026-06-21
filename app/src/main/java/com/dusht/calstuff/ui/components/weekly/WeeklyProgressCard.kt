package com.dusht.calstuff.ui.components.weekly

import com.dusht.calstuff.ui.model.WeeklyProgressConfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dusht.calstuff.ui.theme.FontSize

private val LowColor = Color(0xFFFFD643)   // yellow — low intake
private val HighColor = Color(0xFFF85B4E)  // red — high/over intake

/** Interpolate from yellow to red based on how close calories are to the goal. */
private fun barColorForIntensity(consumed: Int, goal: Int): Color {
    if (goal <= 0) return LowColor
    val ratio = (consumed.toFloat() / goal).coerceIn(0f, 1f)
    return lerp(LowColor, HighColor, ratio)
}

private val BAR_HEIGHT = 160.dp

@Composable
fun WeeklyProgressCard(
    config: WeeklyProgressConfig,
    modifier: Modifier = Modifier
) {
    val maxCal = config.maxCalories

    // Y-axis labels (4 steps from 0 to max)
    val ySteps = 4
    val yLabels = (0..ySteps).map { i ->
        ((maxCal.toFloat() / ySteps) * i).toInt()
    }.reversed()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: title + avg
                Column {
                    Text(
                        text = "My Weekly Progress",
                        color = Color(0xFFBBBBBB),
                        fontSize = FontSize.medium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!config.isEmpty) {
                        Text(
                            text = "Avg ${config.averageCalories} kcal/day",
                            color = Color(0xFFCCCCCC),
                            fontSize = FontSize.xSmall,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Right: daily goal
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Daily Goal",
                        color = Color(0xFFBBBBBB),
                        fontSize = FontSize.xSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${config.calorieGoal} kcal",
                        color = Color(0xFF222222),
                        fontSize = FontSize.mediumLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (config.days.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BAR_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data yet",
                        color = Color(0xFFBBBBBB),
                        fontSize = FontSize.medium
                    )
                }
            } else {
                // Chart: Y-axis + Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Y-axis labels
                    Column(
                        modifier = Modifier.height(BAR_HEIGHT),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        yLabels.forEach { label ->
                            Text(
                                text = label.toString(),
                                color = Color(0xFFBBBBBB),
                                fontSize = FontSize.xxxSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Cylinder bars
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(BAR_HEIGHT),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        config.days.forEach { day ->
                            val fill = if (maxCal > 0) {
                                (day.caloriesConsumed.toFloat() / maxCal).coerceIn(0f, 1f)
                            } else 0f
                            val color = barColorForIntensity(day.caloriesConsumed, config.calorieGoal)

                            CylinderBar(
                                fillFraction = fill,
                                barColor = color,
                                barWidth = 28.dp,
                                barHeight = BAR_HEIGHT
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // X-axis: day names + dates aligned under bars
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Spacer matching Y-axis width
                    Spacer(modifier = Modifier.width(34.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        config.days.forEach { day ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.dayName,
                                    color = Color(0xFF888888),
                                    fontSize = FontSize.xxSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = day.date,
                                    color = Color(0xFFBBBBBB),
                                    fontSize = FontSize.xxxSmall,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 380)
@Composable
private fun PreviewWeeklyProgressCard() {
    WeeklyProgressCard(
        config = WeeklyProgressConfig.mock(),
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 380)
@Composable
private fun PreviewWeeklyProgressCardEmpty() {
    WeeklyProgressCard(
        config = WeeklyProgressConfig.empty(),
        modifier = Modifier.padding(16.dp)
    )
}
