package com.dusht.calstuff.ui.components.streak

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.dusht.calstuff.ui.theme.FontSize
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Square streak card — use with Modifier.weight(1f) to take half screen width.
 * Left: streak number. Right: fire Lottie animation.
 *
 * Setup: place a fire Lottie JSON at app/src/main/assets/fire.json
 * Download from: https://lottiefiles.com/search?q=fire
 */
@Composable
fun StreakCard(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clipToBounds(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Box(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 6.dp)
        ) {
            // Text — left side
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "day streak",
                    color = Color(0xFFBBBBBB),
                    fontSize = FontSize.xxxLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = streakDays.toString(),
                    color = Color(0xFF222222),
                    fontSize = FontSize.display1,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 72.sp
                )
            }

            // Fire animation — right side, oversized, clipped by card
            FireAnimation(
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 50.dp, y = (-10).dp)
            )
        }
    }
}

@Composable
private fun FireAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("fire.json")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    if (composition != null) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\uD83D\uDD25", fontSize = FontSize.displayHero)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 180)
@Composable
private fun PreviewStreakCard() {
    StreakCard(
        streakDays = 28,
        modifier = Modifier.padding(16.dp)
    )
}
