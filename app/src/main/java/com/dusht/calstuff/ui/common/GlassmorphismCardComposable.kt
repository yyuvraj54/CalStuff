package com.dusht.calstuff.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    backgroundAlpha1: Float = 0.12f,
    backgroundAlpha2: Float = 0.06f,
    blurRadius: Dp = 25.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = backgroundAlpha1),
                                Color.White.copy(alpha = backgroundAlpha2)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blurRadius)
                    .background(
                        Color.White.copy(alpha = 0.04f),
                        RoundedCornerShape(cornerRadius)
                    )
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}
