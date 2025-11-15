package com.dusht.calstuff.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MealData(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fiber: Float,
    val fats: Float,
    val monounsaturatedFat: Float,
    val polyunsaturatedFat: Float,
    val saturatedFat: Float,
    val sugar: Float
)

@Composable
fun NutritionalAnalysisCard(meal: MealData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF2B2B2B),
                RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        // Title - Food name and calories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = meal.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false)
            )

            Text(
                text = "${meal.calories} kcal",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Top row - Energy, Carbs, Fat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutrientItem(
                label = "Energy",
                value = "${meal.calories}",
                unit = "kcal",
                modifier = Modifier.weight(1f)
            )
            NutrientItem(
                label = "Carbohydrates",
                value = "${meal.carbs.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
            NutrientItem(
                label = "Fat (Total)",
                value = "${meal.fats.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Middle row - Fiber, Protein, Sugar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutrientItem(
                label = "Fiber",
                value = "${meal.fiber.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
            NutrientItem(
                label = "Protein",
                value = "${meal.protein.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
            NutrientItem(
                label = "Sugar",
                value = "${meal.sugar.toInt()}",
                unit = "g",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Fat breakdown rows
        FatBreakdownRow(
            label = "Fat (Monounsaturated)",
            value = "${meal.monounsaturatedFat.toInt()} g"
        )

        Spacer(modifier = Modifier.height(16.dp))

        FatBreakdownRow(
            label = "Fat (Polyunsaturated)",
            value = "${meal.polyunsaturatedFat.toInt()} g"
        )

        Spacer(modifier = Modifier.height(16.dp))

        FatBreakdownRow(
            label = "Fat (Saturated)",
            value = "${meal.saturatedFat.toInt()} g"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Footer text
        Text(
            text = "Based on 1.0x portion. One portion is two bhature",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun NutrientItem(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
fun FatBreakdownRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewNutritionalAnalysisCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        val sampleMeals = listOf(
            MealData(
                name = "Grilled Chicken Salad",
                calories = 450,
                protein = 35f,
                carbs = 20f,
                fiber = 8f,
                fats = 22f,
                monounsaturatedFat = 10f,
                polyunsaturatedFat = 6f,
                saturatedFat = 6f,
                sugar = 5f
            ),
            MealData(
                name = "Oatmeal with Berries",
                calories = 320,
                protein = 12f,
                carbs = 58f,
                fiber = 12f,
                fats = 8f,
                monounsaturatedFat = 3f,
                polyunsaturatedFat = 3f,
                saturatedFat = 2f,
                sugar = 15f
            ),
            MealData(
                name = "Salmon with Vegetables",
                calories = 520,
                protein = 42f,
                carbs = 15f,
                fiber = 6f,
                fats = 32f,
                monounsaturatedFat = 14f,
                polyunsaturatedFat = 12f,
                saturatedFat = 6f,
                sugar = 4f
            ),
            MealData(
                name = "Greek Yogurt Bowl",
                calories = 280,
                protein = 18f,
                carbs = 35f,
                fiber = 4f,
                fats = 10f,
                monounsaturatedFat = 4f,
                polyunsaturatedFat = 2f,
                saturatedFat = 4f,
                sugar = 20f
            )
        )

        // Display first meal as example
        NutritionalAnalysisCard(meal = sampleMeals[0])
    }
}

//@Composable
//fun MealItem(meal: MealData) {
//    Column(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = meal.name,
//                color = Color(0xFF1E1E1E),
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                modifier = Modifier.weight(1f)
//            )
//
//            Box(
//                modifier = Modifier
//                    .background(
//                        Color(0xFFff6b6b).copy(alpha = 0.25f),
//                        RoundedCornerShape(10.dp)
//                    )
//                    .padding(horizontal = 12.dp, vertical = 6.dp)
//            ) {
//                Text(
//                    text = "${meal.calories} cal",
//                    color = Color(0xFFff6b6b),
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // Macronutrients row
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            MacroItem(
//                label = "Protein",
//                value = meal.protein,
//                color = Color(0xFF4ecdc4)
//            )
//            MacroItem(
//                label = "Carbs",
//                value = meal.carbs,
//                color = Color(0xFFffe66d)
//            )
//            MacroItem(
//                label = "Fiber",
//                value = meal.fiber,
//                color = Color(0xFF95e1d3)
//            )
//            MacroItem(
//                label = "Fats",
//                value = meal.fats,
//                color = Color(0xFFf38181)
//            )
//        }
//    }
//}

//@Composable
//fun MacroItem(
//    label: String,
//    value: Float,
//    color: Color
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.width(70.dp)
//    ) {
//        Text(
//            text = label,
//            color = Color.Gray.copy(alpha = 0.6f),
//            fontSize = 11.sp,
//            fontWeight = FontWeight.Medium
//        )
//
//        Spacer(modifier = Modifier.height(6.dp))
//
//        Box(
//            modifier = Modifier
//                .background(
//                    color.copy(alpha = 0.2f),
//                    RoundedCornerShape(8.dp)
//                )
//                .padding(horizontal = 10.dp, vertical = 6.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "${value}g",
//                color = color,
//                fontSize = 13.sp,
//                fontWeight = FontWeight.Bold
//            )
//        }
//    }
//}


@Preview(showBackground = true, backgroundColor = 0xFF1a1a2e)
@Composable
fun PreviewCustomGlassmorphismCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e)
                    )
                )
            )
            .padding(16.dp)
    ) {
        GlassmorphismCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            cornerRadius = 20.dp,
            backgroundAlpha1 = 0.15f,
            backgroundAlpha2 = 0.08f
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Custom Glassmorphism Card",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}