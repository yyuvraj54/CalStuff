package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dusht.calstuff.ui.common.MealData
import com.dusht.calstuff.ui.common.NutritionalAnalysisCard
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFFF8F9FA))
                .fillMaxSize(),
        ) {
            StreakCalorieCard(
                streakDays = 28,
                caloriesConsumed = 1850,
                totalCalories = 2200,
                caloriesLeft = 350
            )

            val sampleMeals = listOf(
                MealData(name = "Grilled Chicken Salad", calories = 450, protein = 35f, carbs = 20f, fiber = 8f, fats = 22f, monounsaturatedFat = 10f, polyunsaturatedFat = 6f, saturatedFat = 6f, sugar = 5f),
                MealData(name = "Oatmeal with Berries", calories = 320, protein = 12f, carbs = 58f, fiber = 12f, fats = 8f, monounsaturatedFat = 3f, polyunsaturatedFat = 3f, saturatedFat = 2f, sugar = 15f),
                MealData(name = "Salmon with Vegetables", calories = 520, protein = 42f, carbs = 15f, fiber = 6f, fats = 32f, monounsaturatedFat = 14f, polyunsaturatedFat = 12f, saturatedFat = 6f, sugar = 4f),
                MealData(name = "Greek Yogurt Bowl", calories = 280, protein = 18f, carbs = 35f, fiber = 4f, fats = 10f, monounsaturatedFat = 4f, polyunsaturatedFat = 2f, saturatedFat = 4f, sugar = 20f)
            )

            MealListCard(sampleMeals, modifier = Modifier)
        }
    }
}
@Composable
fun StreakCalorieCard(
    streakDays: Int,
    caloriesConsumed: Int,
    totalCalories: Int,
    caloriesLeft: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Soft gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE3F2FD),
                                Color(0xFFF3E5F5)
                            )
                        ),
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(30.dp)
                    .background(
                        Color(0xFFE8EAF6).copy(alpha = 0.3f),
                    )
            )

            // Content
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Circular streak indicator with animated progress
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .offset(x = (10).dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedCircularProgress(
                        streakDays = streakDays,
                        caloriesConsumed = caloriesConsumed,
                        totalCalories = totalCalories
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Right side - Calorie information
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalorieLabel(
                        label = "Calories Consumed",
                        value = caloriesConsumed,
                        color = Color(0xFFE53935)
                    )
                    CalorieLabel(
                        label = "Total Calories",
                        value = totalCalories,
                        color = Color(0xFF00ACC1)
                    )
                    CalorieLabel(
                        label = "Calories Left",
                        value = caloriesLeft,
                        color = Color(0xFF43A047)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedCircularProgress(
    streakDays: Int,
    caloriesConsumed: Int,
    totalCalories: Int
) {
    // Calculate progress (0f to 1f)
    val targetProgress = (caloriesConsumed.toFloat() / totalCalories.toFloat()).coerceIn(0f, 1f)

    // Animate progress on first appearance - using remember to reset on recomposition
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isInitialized = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isInitialized) targetProgress else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "progress"
    )

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF6F00).copy(alpha = 0.15f),
                            Color(0xFFFF8F00).copy(alpha = 0.08f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Circular progress indicator
        Canvas(
            modifier = Modifier.size(140.dp)
        ) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension
            val radius = (diameter / 2f) - (strokeWidth / 2f)

            // Background track (light gray circle)
            drawCircle(
                color = Color(0xFFE0E0E0).copy(alpha = 0.3f),
                radius = radius,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc with smooth orange gradient
            if (animatedProgress > 0f) {
                val sweepAngle = animatedProgress * 360f

                // Outer glow layer
                drawArc(
                    color = Color(0xFFFF6F00).copy(alpha = 0.3f),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth + 6.dp.toPx(), cap = StrokeCap.Round),
                    alpha = 0.4f
                )

                // Main progress arc with smooth orange
                drawArc(
                    color = Color(0xFFFF6F00),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val angleInRadians = Math.toRadians((-90f + sweepAngle).toDouble())
                val endX = center.x + (radius * cos(angleInRadians)).toFloat() - (strokeWidth / 2f)
                val endY = center.y + (radius * sin(angleInRadians)).toFloat() - (strokeWidth / 2f)
                // Outer glow of end cap
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to Color(0xFFFF8F00).copy(alpha = 0.8f),
                        0.5f to Color(0xFFFF6F00).copy(alpha = 0.4f),
                        1f to Color.Transparent
                    ),
                    radius = strokeWidth * 1.5f,
                    center = Offset(endX, endY)
                )

                // Inner bright spot of end cap
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = strokeWidth / 2,
                    center = Offset(endX, endY)
                )

                // Middle layer of end cap
                drawCircle(
                    color = Color(0xFFFFAB40),
                    radius = strokeWidth / 1.5f,
                    center = Offset(endX, endY)
                )
            }
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STREAK",
                color = Color(0xFFFF6F00),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = streakDays.toString(),
                color = Color(0xFFE65100),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CalorieLabel(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value.toString(),
            fontSize = 16.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun MealListCard(
    meals: List<MealData>,
    title: String = "Today Meals",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .heightIn(min = 200.dp, max = 500.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Soft gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFF3E0).copy(alpha = 0.5f),
                                Color(0xFFE1F5FE).copy(alpha = 0.5f)
                            )
                        ),
                    )
            )

            // Blur effect overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(25.dp)
                    .background(
                        Color(0xFFF5F5F5).copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = title,
                    color = Color(0xFF263238),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Meal List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(meals) { meal ->
                        NutritionalAnalysisCard(meal = meal)

                        if (meal != meals.last()) {
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFBDBDBD).copy(alpha = 0.3f),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(Modifier.padding(0.dp))
}