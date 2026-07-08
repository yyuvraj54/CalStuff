package com.dusht.calstuff.ui.components.streak

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import com.airbnb.lottie.compose.LottieAnimation
import com.dusht.calstuff.ui.theme.FontSize
import com.dusht.calstuff.ui.theme.calStuffColors
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
    val hasActiveStreak = streakDays > 0
    val colors = MaterialTheme.calStuffColors

    Card(
        modifier = modifier.clipToBounds(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 6.dp)
        ) {
            // Text — left side
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
                if (hasActiveStreak) {
                    Text(
                        text = "day streak",
                        color = colors.textSecondary,
                        fontSize = FontSize.xxxLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = streakDays.toString(),
                        color = colors.textPrimary,
                        fontSize = FontSize.display1,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 72.sp
                    )
                } else {
                    Text(
                        text = "Start your streak",
                        color = colors.textPrimary,
                        fontSize = FontSize.xLarge,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Log a meal today\nto get started",
                        color = colors.textSecondary,
                        fontSize = FontSize.xSmall,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    )
                }
            }

            // Fire animation — right side, oversized, clipped by card
            val fireModifier = Modifier
                .size(190.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = (-10).dp)
            if (hasActiveStreak) {
                FireAnimation(modifier = fireModifier)
            } else {
                InactiveFireAnimation(modifier = fireModifier)
            }
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

/**
 * Greyscale/unlit fire GIF shown while there's no active streak.
 * Asset: app/src/main/assets/streak_inactive_fire.gif
 */
@Composable
private fun InactiveFireAnimation(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val gifImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
    AsyncImage(
        model = "file:///android_asset/streak_inactive_fire.gif",
        contentDescription = null,
        imageLoader = gifImageLoader,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 180)
@Composable
private fun PreviewStreakCard() {
    StreakCard(
        streakDays = 28,
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 180)
@Composable
private fun PreviewStreakCardNoStreak() {
    StreakCard(
        streakDays = 0,
        modifier = Modifier.padding(16.dp)
    )
}
