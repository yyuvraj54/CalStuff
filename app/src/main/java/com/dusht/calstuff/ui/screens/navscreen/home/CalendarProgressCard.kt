package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun CalendarProgressCard(
    percentage: Int,
    description: String,
    year: Int,
    month: Int, // 0-based (Calendar.JANUARY = 0)
    highlightedDays: Set<Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title row with month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress Statistics",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                val monthName = remember(year, month) {
                    val cal = Calendar.getInstance()
                    cal.set(year, month, 1)
                    cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: ""
                }
                Text(
                    text = monthName,
                    color = AppYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Percentage row
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$percentage%",
                    color = AppYellow,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 64.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Calendar grid
            MonthCalendarGrid(
                year = year,
                month = month,
                highlightedDays = highlightedDays
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFECF4F, widthDp = 380)
@Composable
private fun PreviewCalendarProgressCard() {
    val cal = Calendar.getInstance()
    CalendarProgressCard(
        percentage = 45,
        description = "of the weekly plan\ncompleted",
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH),
        highlightedDays = setOf(1, 2, 5, 6, 8, 9, 12, 13, 15),
        modifier = Modifier.padding(16.dp)
    )
}
