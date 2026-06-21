package com.dusht.calstuff.ui.components.weekly

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CylinderBar(
    fillFraction: Float,
    barColor: Color,
    modifier: Modifier = Modifier,
    barWidth: Dp = 28.dp,
    barHeight: Dp = 140.dp,
    ovalHeight: Dp = 12.dp
) {
    val clampedFill = fillFraction.coerceIn(0f, 1f)

    // Animate fill from 0 to target
    var targetFill by remember { mutableStateOf(0f) }
    LaunchedEffect(clampedFill) {
        targetFill = clampedFill
    }
    val animatedFill by animateFloatAsState(
        targetValue = targetFill,
        animationSpec = tween(durationMillis = 900, delayMillis = 150),
        label = "fill"
    )

    // Floating bob animation for the top cap — only runs when there's fill
    val infiniteTransition = rememberInfiniteTransition(label = "bob")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (clampedFill > 0f) 3f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobOffset"
    )

    // 3D shading colors
    val darkShade = barColor.copy(alpha = 0.65f)
    val lightShade = barColor
    val highlightShade = barColor.copy(alpha = 0.35f)
    val capLight = barColor.copy(alpha = 0.9f)
    val tubeBackground = Color(0xFFEAEAEA)
    val tubeDark = Color(0xFFD8D8D8)
    val tubeLight = Color(0xFFF2F2F2)

    Canvas(
        modifier = modifier
            .width(barWidth)
            .height(barHeight)
    ) {
        val w = size.width
        val h = size.height
        val ovalH = ovalHeight.toPx()
        val halfOval = ovalH / 2f

        // Usable body area (between top and bottom ovals)
        val bodyTop = halfOval
        val bodyBottom = h - halfOval
        val bodyHeight = bodyBottom - bodyTop

        if (bodyHeight <= 0f) return@Canvas // safety: too small to draw

        val fillHeight = bodyHeight * animatedFill
        val fillTop = bodyBottom - fillHeight

        // === Empty tube ===

        // Tube body with 3D gradient
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(tubeDark, tubeLight, tubeDark),
                startX = 0f,
                endX = w
            ),
            topLeft = Offset(0f, bodyTop),
            size = Size(w, bodyHeight)
        )

        // Bottom base oval
        drawOval(
            color = tubeDark,
            topLeft = Offset(0f, bodyBottom - halfOval),
            size = Size(w, ovalH)
        )

        // Top rim oval of empty tube
        drawOval(
            brush = Brush.horizontalGradient(
                colors = listOf(tubeDark, tubeLight, tubeDark)
            ),
            topLeft = Offset(0f, bodyTop - halfOval),
            size = Size(w, ovalH)
        )

        // Inner top rim (hole effect)
        val rimInset = w * 0.12f
        drawOval(
            color = tubeDark.copy(alpha = 0.5f),
            topLeft = Offset(rimInset, bodyTop - halfOval * 0.6f),
            size = Size(w - rimInset * 2, ovalH * 0.6f)
        )

        // === Filled portion ===
        if (animatedFill > 0.005f) {
            // Ensure minimum visible fill height for very small values
            val visibleFillTop = fillTop.coerceAtMost(bodyBottom - halfOval)

            // Filled body — 3D horizontal gradient
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(darkShade, lightShade, highlightShade, darkShade),
                    startX = 0f,
                    endX = w
                ),
                topLeft = Offset(0f, visibleFillTop),
                size = Size(w, bodyBottom - visibleFillTop)
            )

            // Bottom oval of filled (covers base)
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(darkShade, lightShade, darkShade)
                ),
                topLeft = Offset(0f, bodyBottom - halfOval),
                size = Size(w, ovalH)
            )

            // Floating top cap with bob
            val effectiveBob = if (animatedFill > 0.05f) bobOffset else 0f
            val capY = (visibleFillTop - halfOval + effectiveBob).coerceAtLeast(-halfOval)

            // Cap shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.08f),
                topLeft = Offset(0f, capY + 2f),
                size = Size(w, ovalH)
            )

            // Main cap
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(darkShade, capLight, highlightShade)
                ),
                topLeft = Offset(0f, capY),
                size = Size(w, ovalH)
            )

            // Glossy highlight on cap
            drawOval(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(w * 0.18f, capY + ovalH * 0.18f),
                size = Size(w * 0.5f, ovalH * 0.45f)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewCylinderBarHigh() {
    CylinderBar(fillFraction = 0.85f, barColor = Color(0xFFFFD643), barHeight = 150.dp)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewCylinderBarMid() {
    CylinderBar(fillFraction = 0.45f, barColor = Color(0xFFF85B4E), barHeight = 150.dp)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewCylinderBarLow() {
    CylinderBar(fillFraction = 0.08f, barColor = Color(0xFF222222), barHeight = 150.dp)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewCylinderBarEmpty() {
    CylinderBar(fillFraction = 0f, barColor = Color(0xFFFFD643), barHeight = 150.dp)
}
