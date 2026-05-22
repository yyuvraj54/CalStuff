package com.dusht.calstuff.ui.screens.navscreen.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

val ProteinColor = Color(0xFFFFD643)
val CarbsColor = Color(0xFFF85B4E)
val FatColor = Color(0xFF222222)

private const val GAP_DEGREES = 8f
private const val START_ANGLE = -90f

@Composable
fun NutritionRing(
    config: DailyNutritionConfig,
    modifier: Modifier = Modifier,
    ringSize: Dp = 170.dp,
    ringThickness: Dp = 30.dp,
    cornerRadius: Dp = 8.dp
) {
    val total = config.proteinConsumed + config.carbsConsumed + config.fatConsumed
    val hasData = total > 0f

    val minSweep = 12f // minimum visible arc degrees
    val availableDegrees = 360f - (GAP_DEGREES * 3)
    // Count how many nutrients have data
    val activeCount = listOf(config.proteinConsumed, config.carbsConsumed, config.fatConsumed).count { it > 0f }
    // Reserve minimum sweep for small segments, distribute rest proportionally
    val rawProtein = if (hasData) (config.proteinConsumed / total) * availableDegrees else 0f
    val rawCarbs = if (hasData) (config.carbsConsumed / total) * availableDegrees else 0f
    val rawFat = if (hasData) (config.fatConsumed / total) * availableDegrees else 0f

    fun adjustSweep(raw: Float, consumed: Float): Float {
        if (consumed <= 0f) return 0f
        return raw.coerceAtLeast(minSweep)
    }

    val proteinSweepRaw = adjustSweep(rawProtein, config.proteinConsumed)
    val carbsSweepRaw = adjustSweep(rawCarbs, config.carbsConsumed)
    val fatSweepRaw = adjustSweep(rawFat, config.fatConsumed)

    // Normalize so total doesn't exceed availableDegrees
    val sweepTotal = proteinSweepRaw + carbsSweepRaw + fatSweepRaw
    val scale = if (sweepTotal > 0f) availableDegrees / sweepTotal else 1f
    val proteinSweep = proteinSweepRaw * scale
    val carbsSweep = carbsSweepRaw * scale
    val fatSweep = fatSweepRaw * scale

    // Animated progress for each segment (0f → 1f with bounce)
    val proteinAnim = remember { Animatable(0f) }
    val carbsAnim = remember { Animatable(0f) }
    val fatAnim = remember { Animatable(0f) }

    LaunchedEffect(config) {
        if (!hasData) return@LaunchedEffect

        // Reset
        proteinAnim.snapTo(0f)
        carbsAnim.snapTo(0f)
        fatAnim.snapTo(0f)

        // Staggered bounce animation — each starts after the previous
        proteinAnim.animateTo(
            1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
        )
        carbsAnim.animateTo(
            1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
        )
        fatAnim.animateTo(
            1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
        )
    }

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val thickness = ringThickness.toPx()
            val cr = cornerRadius.toPx()
            val outerR = size.minDimension / 2f
            val innerR = outerR - thickness
            val c = Offset(size.width / 2f, size.height / 2f)

            if (!hasData) {
                drawRoundedSegment(c, innerR, outerR, 0f, 360f, cr, Color(0xFFE0E0E0))
                return@Canvas
            }

            // Full ring in widget background color so gaps blend in
            drawRoundedSegment(c, innerR, outerR, 0f, 360f, cr, Color.White)

            val animatedSweeps = listOf(
                (proteinSweep * proteinAnim.value) to ProteinColor,
                (carbsSweep * carbsAnim.value) to CarbsColor,
                (fatSweep * fatAnim.value) to FatColor
            )

            var currentAngle = START_ANGLE

            for ((sweep, color) in animatedSweeps) {
                val targetSweep = sweep.coerceAtLeast(0f)
                if (targetSweep > 1f) {
                    drawRoundedSegment(c, innerR, outerR, currentAngle, targetSweep, cr, color)
                }
                // Advance by full target sweep (not animated) so positions stay correct
                currentAngle += when (color) {
                    ProteinColor -> proteinSweep
                    CarbsColor -> carbsSweep
                    else -> fatSweep
                } + GAP_DEGREES
            }
        }

        // Percentage bubbles on the ring
        if (hasData) {
            val density = LocalDensity.current
            val ringSizePx = with(density) { ringSize.toPx() }
            val thicknessPx = with(density) { ringThickness.toPx() }
            // Bubble sits on the outer edge of the ring (midpoint of ring thickness from outside)
            val bubbleRadius = ringSizePx / 2f - thicknessPx / 2f
            val bubbleSizeDp = 38.dp

            data class BubbleData(
                val sweep: Float,
                val startAngle: Float,
                val color: Color,
                val percent: Int,
                val anim: Animatable<Float, *>
            )

            var angle = START_ANGLE
            val bubbles = listOf(
                BubbleData(proteinSweep, angle, ProteinColor, (config.proteinPercent * 100).toInt(), proteinAnim).also { angle += proteinSweep + GAP_DEGREES },
                BubbleData(carbsSweep, angle, CarbsColor, (config.carbsPercent * 100).toInt(), carbsAnim).also { angle += carbsSweep + GAP_DEGREES },
                BubbleData(fatSweep, angle, FatColor, (config.fatPercent * 100).toInt(), fatAnim)
            )

            bubbles.forEach { bubble ->
                if (bubble.sweep > 0f && bubble.anim.value > 0.1f) {
                    // Position at midpoint of the segment arc, from center of ring
                    val midAngle = bubble.startAngle + (bubble.sweep * bubble.anim.value) / 2f
                    val rad = Math.toRadians(midAngle.toDouble())
                    val cx = bubbleRadius * cos(rad).toFloat()
                    val cy = bubbleRadius * sin(rad).toFloat()

                    // Offset from center (parent has Alignment.Center)
                    val offsetX = with(density) { cx.toDp() }
                    val offsetY = with(density) { cy.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .scale(bubble.anim.value.coerceIn(0f, 1f))
                            .size(bubbleSizeDp)
                            .clip(CircleShape)
                            .background(bubble.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${bubble.percent}%",
                            color = if (bubble.color == FatColor) Color.White else Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Animated kcal counter — tracks overall ring fill progress
        val overallProgress = if (hasData) {
            val weightedProgress =
                (proteinAnim.value * config.proteinConsumed +
                 carbsAnim.value * config.carbsConsumed +
                 fatAnim.value * config.fatConsumed) / total
            weightedProgress.coerceIn(0f, 1f)
        } else 0f
        val animatedKcal = (config.caloriesConsumed * overallProgress).toInt()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = animatedKcal.toString(),
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            )
            Text(
                text = "/kcal",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * Draws a single arc segment shaped like a rounded rectangle bent into a circle.
 * Four smooth corners connect the outer arc, inner arc, and radial edges.
 */
private fun DrawScope.drawRoundedSegment(
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    startAngleDeg: Float,
    sweepAngleDeg: Float,
    cornerRadius: Float,
    color: Color
) {
    // If sweep is full circle, just draw two concentric circles
    if (sweepAngleDeg >= 360f) {
        val path = Path().apply {
            addOval(Rect(center.x - outerRadius, center.y - outerRadius, center.x + outerRadius, center.y + outerRadius))
            addOval(Rect(center.x - innerRadius, center.y - innerRadius, center.x + innerRadius, center.y + innerRadius))
        }
        // Use even-odd fill to cut out the center
        drawPath(path, color, style = Fill)
        return
    }

    val endAngleDeg = startAngleDeg + sweepAngleDeg

    // Limit corner radius so corners don't exceed half the sweep or half the thickness
    val maxCornerForThickness = (outerRadius - innerRadius) / 2f
    val maxOuterCornerDeg = sweepAngleDeg / 4f // each corner eats at most 1/4 of sweep
    val maxOuterCornerPx = (Math.toRadians(maxOuterCornerDeg.toDouble()) * outerRadius).toFloat()
    val cr = cornerRadius.coerceAtMost(maxCornerForThickness).coerceAtMost(maxOuterCornerPx)

    val outerCornerDeg = Math.toDegrees((cr / outerRadius).toDouble()).toFloat()
    val innerCornerDeg = Math.toDegrees((cr / innerRadius).toDouble()).toFloat()

    fun pt(radius: Float, angleDeg: Float): Offset {
        val rad = Math.toRadians(angleDeg.toDouble())
        return Offset(
            center.x + radius * cos(rad).toFloat(),
            center.y + radius * sin(rad).toFloat()
        )
    }

    val outerRect = Rect(
        center.x - outerRadius, center.y - outerRadius,
        center.x + outerRadius, center.y + outerRadius
    )
    val innerRect = Rect(
        center.x - innerRadius, center.y - innerRadius,
        center.x + innerRadius, center.y + innerRadius
    )

    val path = Path().apply {
        // Start: outer arc, slightly past start angle (after corner A)
        val startPt = pt(outerRadius, startAngleDeg + outerCornerDeg)
        moveTo(startPt.x, startPt.y)

        // 1. Outer arc → from start+corner to end-corner
        arcTo(outerRect, startAngleDeg + outerCornerDeg, sweepAngleDeg - 2 * outerCornerDeg, false)

        // 2. Corner B (outer-end): outer arc → radial inward
        val bControl = pt(outerRadius, endAngleDeg)
        val bTarget = pt(outerRadius - cr, endAngleDeg)
        quadraticBezierTo(bControl.x, bControl.y, bTarget.x, bTarget.y)

        // 3. Radial line inward at end angle
        val radialEndInner = pt(innerRadius + cr, endAngleDeg)
        lineTo(radialEndInner.x, radialEndInner.y)

        // 4. Corner C (inner-end): radial → inner arc
        val cControl = pt(innerRadius, endAngleDeg)
        val cTarget = pt(innerRadius, endAngleDeg - innerCornerDeg)
        quadraticBezierTo(cControl.x, cControl.y, cTarget.x, cTarget.y)

        // 5. Inner arc (counter-clockwise) → from end-corner back to start+corner
        arcTo(innerRect, endAngleDeg - innerCornerDeg, -(sweepAngleDeg - 2 * innerCornerDeg), false)

        // 6. Corner D (inner-start): inner arc → radial outward
        val dControl = pt(innerRadius, startAngleDeg)
        val dTarget = pt(innerRadius + cr, startAngleDeg)
        quadraticBezierTo(dControl.x, dControl.y, dTarget.x, dTarget.y)

        // 7. Radial line outward at start angle
        val radialStartOuter = pt(outerRadius - cr, startAngleDeg)
        lineTo(radialStartOuter.x, radialStartOuter.y)

        // 8. Corner A (outer-start): radial → outer arc
        val aControl = pt(outerRadius, startAngleDeg)
        val aTarget = pt(outerRadius, startAngleDeg + outerCornerDeg)
        quadraticBezierTo(aControl.x, aControl.y, aTarget.x, aTarget.y)

        close()
    }

    drawPath(path, color, style = Fill)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewNutritionRing() {
    NutritionRing(config = DailyNutritionConfig.mock())
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewNutritionRingEmpty() {
    NutritionRing(
        config = DailyNutritionConfig(
            dailyCalorieGoal = 2200,
            caloriesConsumed = 0,
            proteinConsumed = 0f, proteinGoal = 150f,
            carbsConsumed = 0f, carbsGoal = 250f,
            fatConsumed = 0f, fatGoal = 70f
        )
    )
}
