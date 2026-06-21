package com.dusht.calstuff.ui.components.bmi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dusht.calstuff.ui.model.BmiConfig
import com.dusht.calstuff.ui.theme.FontSize
import kotlin.math.cos
import kotlin.math.sin

private const val GAUGE_START = 180f
private const val GAUGE_SWEEP = 180f
private const val BMI_MIN = 10f
private const val BMI_MAX = 40f

private val ZONES = listOf(
    Triple(0f, 0.283f, Color(0xFF42A5F5)),
    Triple(0.283f, 0.50f, Color(0xFF66BB6A)),
    Triple(0.50f, 0.667f, Color(0xFFFFD643)),
    Triple(0.667f, 1f, Color(0xFFF85B4E))
)

@Composable
fun BmiGauge(
    config: BmiConfig,
    modifier: Modifier = Modifier,
    gaugeSize: Dp = 200.dp
) {
    val bmi = config.bmiValue.coerceIn(BMI_MIN, BMI_MAX)

    val needleProgress = remember { Animatable(0f) }
    LaunchedEffect(bmi) {
        needleProgress.snapTo(0f)
        val targetFraction = ((bmi - BMI_MIN) / (BMI_MAX - BMI_MIN)).coerceIn(0f, 1f)
        needleProgress.animateTo(
            targetFraction,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
        )
    }

    Column(
        modifier = modifier.width(gaugeSize),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gauge canvas — just the semicircle + needle
        Box(
            modifier = Modifier.size(gaugeSize, gaugeSize / 2 + 8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.size(gaugeSize, gaugeSize / 2 + 8.dp)) {
                val strokeWidth = 20.dp.toPx()
                val padding = strokeWidth / 2f + 6f
                val arcSize = Size(size.width - padding * 2, size.width - padding * 2)
                val topLeft = Offset(padding, padding)
                val centerX = size.width / 2f
                val centerY = arcSize.height / 2f + padding

                // Colored zones with gaps
                ZONES.forEach { (start, end, color) ->
                    val startAngle = GAUGE_START + start * GAUGE_SWEEP
                    val sweep = (end - start) * GAUGE_SWEEP
                    drawArc(
                        color = color,
                        startAngle = startAngle + 1.5f,
                        sweepAngle = sweep - 3f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = arcSize,
                        topLeft = topLeft
                    )
                }

                // Needle — shorter, doesn't reach center
                val needleAngle = GAUGE_START + needleProgress.value * GAUGE_SWEEP
                val needleRad = Math.toRadians(needleAngle.toDouble())
                val needleOuterRadius = arcSize.width / 2f - strokeWidth - 4.dp.toPx()
                val needleInnerRadius = arcSize.width / 2f * 0.25f // starts 25% from center

                val tipX = centerX + needleOuterRadius * cos(needleRad).toFloat()
                val tipY = centerY + needleOuterRadius * sin(needleRad).toFloat()
                val baseX = centerX + needleInnerRadius * cos(needleRad).toFloat()
                val baseY = centerY + needleInnerRadius * sin(needleRad).toFloat()

                // Shadow
                drawLine(
                    color = Color.Black.copy(alpha = 0.08f),
                    start = Offset(baseX + 1f, baseY + 1f),
                    end = Offset(tipX + 1f, tipY + 1f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Needle
                drawLine(
                    color = Color(0xFF222222),
                    start = Offset(baseX, baseY),
                    end = Offset(tipX, tipY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center dot
                drawCircle(
                    color = Color(0xFF222222),
                    radius = 7.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(centerX, centerY)
                )

                // Tip dot
                drawCircle(
                    color = Color(config.category.colorHex),
                    radius = 5.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BMI value — below the gauge, no overlap
        Text(
            text = String.format("%.1f", config.bmiValue),
            fontSize = FontSize.display3,
            fontWeight = FontWeight.Bold,
            color = Color(config.category.colorHex)
        )

        // Category meaning
        Text(
            text = config.category.label,
            fontSize = FontSize.small,
            fontWeight = FontWeight.SemiBold,
            color = Color(config.category.colorHex)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Explanation — age/gender aware
        Text(
            text = config.healthNote,
            fontSize = FontSize.xxSmall,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFAAAAAA)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewBmiGauge() {
    BmiGauge(config = BmiConfig.mock())
}
