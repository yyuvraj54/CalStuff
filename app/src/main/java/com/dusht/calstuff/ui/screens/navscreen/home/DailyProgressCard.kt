package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyProgressCard(
    config: DailyNutritionConfig,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Calories (left) + Daily Goal (right)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Calories",
                    color = Color(0xFFBBBBBB),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(24.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Daily Goal",
                        color = Color(0xFF999999),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                                append("${config.dailyCalorieGoal}")
                            }
                            withStyle(SpanStyle(color = Color(0xFF999999), fontWeight = FontWeight.Normal)) {
                                append(" kcal")
                            }
                        },
                        fontSize = 18.sp
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
                    color = ProteinColor
                )
                NutrientIndicator(
                    label = "Carbs",
                    value = "${config.carbsConsumed.toInt()}g",
                    color = CarbsColor
                )
                NutrientIndicator(
                    label = "Fat",
                    value = "${config.fatConsumed.toInt()}g",
                    color = FatColor
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
        modifier = Modifier.padding(16.dp)
    )
}
