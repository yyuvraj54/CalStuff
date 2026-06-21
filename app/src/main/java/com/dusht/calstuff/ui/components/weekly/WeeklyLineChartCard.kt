package com.dusht.calstuff.ui.components.weekly

import com.dusht.calstuff.ui.model.WeeklyProgressConfig

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dusht.calstuff.ui.theme.FontSize

private val LineLowColor = Color(0xFFFFD643)
private val LineHighColor = Color(0xFFF85B4E)
private val DotColor = Color(0xFF222222)
private val CHART_HEIGHT = 160.dp

@Composable
fun WeeklyLineChartCard(
    config: WeeklyProgressConfig,
    modifier: Modifier = Modifier
) {
    val maxCal = config.maxCalories

    val ySteps = 4
    val yLabels = (0..ySteps).map { i ->
        ((maxCal.toFloat() / ySteps) * i).toInt()
    }.reversed()

    // Animate the line drawing (0 → 1)
    val lineProgress = remember { Animatable(0f) }
    LaunchedEffect(config) {
        lineProgress.snapTo(0f)
        lineProgress.animateTo(
            1f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
        )
    }

    val textMeasurer = rememberTextMeasurer()

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Weekly Trend",
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
                        .height(CHART_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data yet",
                        color = Color(0xFFBBBBBB),
                        fontSize = FontSize.medium
                    )
                }
            } else {
                // Chart: Y-axis + Line graph
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Y-axis labels
                    Column(
                        modifier = Modifier.height(CHART_HEIGHT),
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

                    // Line chart canvas
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(CHART_HEIGHT)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val topPadding = 24f // room for calorie labels above dots
                            val bottomPadding = 8f
                            val chartH = h - topPadding - bottomPadding
                            val dayCount = config.days.size
                            if (dayCount == 0 || maxCal <= 0) return@Canvas

                            // Compute point positions
                            val points = config.days.mapIndexed { index, day ->
                                val x = if (dayCount == 1) w / 2f
                                else w * index / (dayCount - 1).toFloat()
                                val fill = (day.caloriesConsumed.toFloat() / maxCal).coerceIn(0f, 1f)
                                val y = topPadding + chartH * (1f - fill)
                                Offset(x, y)
                            }

                            val progress = lineProgress.value.coerceIn(0f, 1f)

                            // How many points to show based on animation progress
                            val totalSegments = (dayCount - 1).coerceAtLeast(1)
                            val animatedSegments = progress * totalSegments
                            val fullSegments = animatedSegments.toInt()
                            val partialFraction = animatedSegments - fullSegments

                            // Draw connecting line with gradient per segment
                            // Higher Y value (lower on screen) = yellow, lower Y (higher on screen) = red
                            fun colorForY(y: Float): Color {
                                val ratio = 1f - ((y - topPadding) / chartH).coerceIn(0f, 1f)
                                return lerp(LineLowColor, LineHighColor, ratio)
                            }

                            val strokeW = 3.dp.toPx()
                            if (points.size >= 2) {
                                // Draw full segments
                                for (i in 0 until fullSegments.coerceAtMost(points.size - 1)) {
                                    val from = points[i]
                                    val to = points[i + 1]
                                    // Split each segment into small sub-segments for smooth gradient
                                    val subSteps = 12
                                    for (s in 0 until subSteps) {
                                        val t0 = s.toFloat() / subSteps
                                        val t1 = (s + 1).toFloat() / subSteps
                                        val start = Offset(
                                            from.x + (to.x - from.x) * t0,
                                            from.y + (to.y - from.y) * t0
                                        )
                                        val end = Offset(
                                            from.x + (to.x - from.x) * t1,
                                            from.y + (to.y - from.y) * t1
                                        )
                                        val midY = (start.y + end.y) / 2f
                                        drawLine(
                                            color = colorForY(midY),
                                            start = start,
                                            end = end,
                                            strokeWidth = strokeW,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }

                                // Draw partial segment
                                if (fullSegments < points.size - 1 && partialFraction > 0f) {
                                    val from = points[fullSegments]
                                    val to = points[fullSegments + 1]
                                    val subSteps = (12 * partialFraction).toInt().coerceAtLeast(1)
                                    for (s in 0 until subSteps) {
                                        val t0 = s.toFloat() / subSteps * partialFraction
                                        val t1 = (s + 1).toFloat() / subSteps * partialFraction
                                        val start = Offset(
                                            from.x + (to.x - from.x) * t0,
                                            from.y + (to.y - from.y) * t0
                                        )
                                        val end = Offset(
                                            from.x + (to.x - from.x) * t1,
                                            from.y + (to.y - from.y) * t1
                                        )
                                        val midY = (start.y + end.y) / 2f
                                        drawLine(
                                            color = colorForY(midY),
                                            start = start,
                                            end = end,
                                            strokeWidth = strokeW,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }

                            // Draw dots and calorie labels
                            val visibleDots = (fullSegments + 1).coerceAtMost(points.size)
                            for (i in 0 until visibleDots) {
                                val pt = points[i]
                                val day = config.days[i]
                                val intensity = (day.caloriesConsumed.toFloat() / config.calorieGoal).coerceIn(0f, 1f)
                                val dotColor = lerp(LineLowColor, LineHighColor, intensity)

                                // Outer dot
                                drawCircle(
                                    color = dotColor,
                                    radius = 6.dp.toPx(),
                                    center = pt
                                )
                                // Inner dot
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )

                                // Calorie text above dot
                                if (day.caloriesConsumed > 0) {
                                    val label = day.caloriesConsumed.toString()
                                    val textLayout = textMeasurer.measure(
                                        text = label,
                                        style = TextStyle(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF888888)
                                        )
                                    )
                                    val tx = (pt.x - textLayout.size.width / 2f)
                                        .coerceIn(0f, w - textLayout.size.width)
                                    val ty = pt.y - textLayout.size.height - 6.dp.toPx()
                                    drawText(textLayout, topLeft = Offset(tx, ty))
                                }
                            }

                            // Goal dashed line
                            val goalY = topPadding + chartH * (1f - (config.calorieGoal.toFloat() / maxCal).coerceIn(0f, 1f))
                            val dashWidth = 6.dp.toPx()
                            val gapWidth = 4.dp.toPx()
                            var dx = 0f
                            while (dx < w) {
                                val endX = (dx + dashWidth).coerceAtMost(w)
                                drawLine(
                                    color = Color(0xFFDDDDDD),
                                    start = Offset(dx, goalY),
                                    end = Offset(endX, goalY),
                                    strokeWidth = 1.dp.toPx()
                                )
                                dx += dashWidth + gapWidth
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // X-axis labels
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.width(34.dp))
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        config.days.forEach { day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
private fun PreviewWeeklyLineChartCard() {
    WeeklyLineChartCard(
        config = WeeklyProgressConfig.mock(),
        modifier = Modifier.padding(16.dp)
    )
}
