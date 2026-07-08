package com.dusht.calstuff.ui.components.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.dusht.calstuff.ui.components.common.EmptyState
import com.dusht.calstuff.ui.model.DayLog
import com.dusht.calstuff.ui.model.MealType
import com.dusht.calstuff.ui.theme.FontSize
import com.dusht.calstuff.ui.theme.calStuffColors
import java.util.Calendar
import java.util.Locale

@Composable
fun DaySummarySection(
    dayLog: DayLog?,
    selectedDay: Int,
    year: Int,
    month: Int,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.calStuffColors
    Column(modifier = modifier.fillMaxWidth()) {
        // Header: date + total
        val monthName = java.text.DateFormatSymbols(Locale.getDefault()).months[month]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "$monthName $selectedDay, $year",
                color = colors.textPrimary,
                fontSize = FontSize.large,
                fontWeight = FontWeight.Bold
            )
            if (dayLog != null) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.Bold)) {
                            append("${dayLog.totalCalories}")
                        }
                        withStyle(SpanStyle(color = colors.textSecondary, fontWeight = FontWeight.Normal)) {
                            append(" / ${dayLog.calorieGoal} kcal")
                        }
                    },
                    fontSize = FontSize.smallMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (dayLog == null || dayLog.meals.isEmpty()) {
            EmptyState(
                title = "No meals logged",
                subtitle = "Nothing recorded for this day",
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // Group meals by type, in order: Breakfast → Lunch → Dinner → Snacks
            val grouped = dayLog.meals.groupBy { it.mealType }
            val orderedTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACKS)

            orderedTypes.forEach { mealType ->
                val meals = grouped[mealType] ?: return@forEach
                meals.forEach { meal ->
                    MealLogCard(entry = meal)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
