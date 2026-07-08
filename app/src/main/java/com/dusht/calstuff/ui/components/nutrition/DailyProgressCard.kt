package com.dusht.calstuff.ui.components.nutrition

import com.dusht.calstuff.ui.model.DailyNutritionConfig

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dusht.calstuff.ui.theme.FontSize
import com.dusht.calstuff.ui.theme.calStuffColors

@Composable
fun DailyProgressCard(
    config: DailyNutritionConfig,
    dailyCalorieGoal: Int,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.calStuffColors
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Calories (left) + Daily Goal (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Calories",
                    color = colors.textSecondary,
                    fontSize = FontSize.medium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(24.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Daily Goal",
                        color = colors.textSecondary,
                        fontSize = FontSize.xSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.Bold)) {
                                append("$dailyCalorieGoal")
                            }
                            withStyle(SpanStyle(color = colors.textSecondary, fontWeight = FontWeight.Normal)) {
                                append(" kcal")
                            }
                        },
                        fontSize = FontSize.large
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nutrition ring
            NutritionRing(
                config = config,
                ringSize = 190.dp,
                ringThickness = 30.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Legend row
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                NutrientIndicator(
                    label = "Protein",
                    value = "${config.proteinConsumed.toInt()}g",
                    color = colors.proteinColor
                )
                NutrientIndicator(
                    label = "Carbs",
                    value = "${config.carbsConsumed.toInt()}g",
                    color = colors.carbsColor
                )
                NutrientIndicator(
                    label = "Fat",
                    value = "${config.fatConsumed.toInt()}g",
                    color = colors.fatColor
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFECF4F, widthDp = 380)
@Composable
private fun PreviewDailyProgressCard() {
    DailyProgressCard(
        config = DailyNutritionConfig.mock(),
        dailyCalorieGoal = 2200,
        modifier = Modifier.padding(16.dp)
    )
}
