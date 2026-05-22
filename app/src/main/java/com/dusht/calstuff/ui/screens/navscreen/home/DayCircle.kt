package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AppYellow = Color(0xFFFFD643)

private val TodayColor = Color(0xFFFF5252) // bright red

@Composable
fun DayCircle(
    day: Int,
    isHighlighted: Boolean,
    isToday: Boolean = false,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isHighlighted) AppYellow else Color.Black
    val textColor = when {
        isToday -> TodayColor
        isHighlighted -> Color.Black
        else -> AppYellow
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (isToday) Modifier.border(2.dp, TodayColor, CircleShape)
                else Modifier
            )
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = textColor,
            fontSize = (size.value * 0.45f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDayCircleHighlighted() {
    DayCircle(day = 5, isHighlighted = true)
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDayCircleDefault() {
    DayCircle(day = 18, isHighlighted = false)
}
