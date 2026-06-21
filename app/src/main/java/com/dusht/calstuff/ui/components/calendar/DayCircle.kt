package com.dusht.calstuff.ui.components.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AppYellow = Color(0xFFFFD643)

private val TodayColor = Color(0xFFFF5252)
private val CalorieGreen = Color(0xFF66BB6A)
private val CalorieRed = Color(0xFFF85B4E)

/**
 * @param calorieRatio consumed/goal for this day.
 *   - null or 0 = no data, no border
 *   - 0.01–1.0 = partial fill, green→red gradient as it fills
 *   - >1.0 = exceeded, full red border
 */
@Composable
fun DayCircle(
    day: Int,
    isHighlighted: Boolean,
    isToday: Boolean = false,
    isSelected: Boolean = false,
    calorieRatio: Float? = null,
    size: Dp = 36.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> Color.White
        isHighlighted -> AppYellow
        else -> Color.Black
    }
    val textColor = when {
        isSelected -> Color.Black
        isToday -> TodayColor
        isHighlighted -> Color.Black
        else -> AppYellow
    }

    val hasCalorieData = calorieRatio != null && calorieRatio > 0f

    Box(
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // Calorie progress border (drawn behind the circle)
        if (hasCalorieData && calorieRatio != null) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = (this.size.width * 0.08f).coerceAtLeast(2f)
                val padding = strokeWidth / 2f
                val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
                val topLeft = Offset(padding, padding)

                val ratio = calorieRatio.coerceIn(0f, 1.5f)
                val exceeded = calorieRatio > 1f

                // Sweep: ratio maps to 360 degrees (capped at 360)
                val sweepDegrees = (ratio.coerceAtMost(1f) * 360f)

                // Color: green at low fill, transitions to red as approaching full
                val borderColor = if (exceeded) {
                    CalorieRed
                } else {
                    lerp(CalorieGreen, CalorieRed, ratio)
                }

                // Background track (faint)
                drawArc(
                    color = borderColor.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    size = arcSize,
                    topLeft = topLeft
                )

                // Progress arc
                drawArc(
                    color = borderColor,
                    startAngle = -90f,
                    sweepAngle = sweepDegrees,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = arcSize,
                    topLeft = topLeft
                )
            }
        }

        // Today indicator border (on top of calorie border)
        if (isToday && !hasCalorieData) {
            Canvas(modifier = Modifier.size(size)) {
                val sw = 2.dp.toPx()
                val padding = sw / 2f
                drawCircle(
                    color = TodayColor,
                    radius = (this.size.width - sw) / 2f,
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                    style = Stroke(width = sw)
                )
            }
        }

        // Circle background (slightly inset so border is visible)
        val inset = if (hasCalorieData) (size * 0.1f).coerceIn(2.dp, 4.dp) else 0.dp
        val innerSize = size - inset * 2
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            val textSize = (innerSize.value * 0.4f).sp
            Text(
                text = day.toString(),
                color = textColor,
                fontSize = textSize,
                fontWeight = FontWeight.Bold,
                lineHeight = textSize
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDayCircleNoData() {
    DayCircle(day = 5, isHighlighted = true)
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDayCircleLow() {
    DayCircle(day = 10, isHighlighted = true, calorieRatio = 0.3f)
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDayCircleMid() {
    DayCircle(day = 15, isHighlighted = true, calorieRatio = 0.7f)
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDayCircleFull() {
    DayCircle(day = 20, isHighlighted = true, calorieRatio = 1.0f)
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDayCircleExceeded() {
    DayCircle(day = 25, isHighlighted = false, calorieRatio = 1.3f)
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDayCircleToday() {
    DayCircle(day = 1, isHighlighted = false, isToday = true, calorieRatio = 0.5f)
}
