package com.dusht.calstuff.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The app's full semantic color token set for the *current* theme (light or dark).
 * Read it via `MaterialTheme.calStuffColors.xxx` from any composable — never construct or
 * reference a `Color(0xFF...)` literal directly in feature code (see Color.kt).
 */
@Immutable
data class CalStuffColors(
    /** Page/scaffold background. */
    val background: Color,
    /** Card/sheet/dialog background. */
    val surface: Color,
    /** Subtly-recessed surface — chip backgrounds, table-row stripes. */
    val surfaceVariant: Color,
    /** Default body/heading text. */
    val textPrimary: Color,
    /** Muted/secondary text — captions, timestamps, placeholders. */
    val textSecondary: Color,
    /** Hairline dividers and separators. */
    val divider: Color,

    /** Brand accent — buttons, links, selected states, icon tint. */
    val accent: Color,
    /** Content color for anything painted with a solid [accent] fill. */
    val onAccent: Color,
    /** Low-emphasis accent fill — icon chip backgrounds, badges. */
    val accentSoft: Color,
    /** Content color for anything painted with [accentSoft]. */
    val onAccentSoft: Color,

    /** Positive/under-goal status (e.g. calories on track). */
    val success: Color,
    /** Signature brand yellow — progress rings, streak visuals. */
    val highlight: Color,
    /** Content color for anything painted with a solid [highlight] fill — always a fixed dark
     * ink, since the bright yellow needs dark contrast in both themes (unlike [textPrimary],
     * which flips light/dark). */
    val onHighlight: Color,
    /** Informational/neutral status (e.g. "underweight" BMI zone). */
    val info: Color,

    /** Destructive actions (log out, delete, exceeded-goal states). */
    val error: Color,
    /** Content color for anything painted with a solid [error] fill. */
    val onError: Color,
    /** Low-emphasis error fill — e.g. the "Log Out" icon chip background. */
    val errorSoft: Color,
    /** Content color for anything painted with [errorSoft]. */
    val onErrorSoft: Color,

    /** Deliberately-inverted card style (e.g. the black calendar widget) — same in both themes. */
    val inverseSurface: Color,
    /** Primary content color on [inverseSurface]. */
    val onInverseSurface: Color,
    /** Highlight content color on [inverseSurface] (e.g. the yellow percentage figure). */
    val onInverseSurfaceVariant: Color,

    /** Hero gradient start (e.g. profile header, login background). */
    val gradientStart: Color,
    /** Hero gradient end. */
    val gradientEnd: Color,
    /** Text/icon color on top of [gradientStart]-[gradientEnd]. */
    val onGradient: Color,
    /** Muted text/icon color on top of the gradient (subtitles). */
    val onGradientMuted: Color,
)

private val LocalCalStuffColors = staticCompositionLocalOf { LightCalStuffColors }

/** Ergonomic accessor: `MaterialTheme.calStuffColors.accent`, etc. */
val MaterialTheme.calStuffColors: CalStuffColors
    @Composable
    get() = LocalCalStuffColors.current

@Composable
fun CalStuffTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val calStuffColors = if (darkTheme) DarkCalStuffColors else LightCalStuffColors

    // Material3 ColorScheme mirrors the same tokens so any stock Material component
    // (Card, AlertDialog, Switch, ripple color, ...) also follows the active theme.
    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = calStuffColors.accent,
            onPrimary = calStuffColors.onAccent,
            primaryContainer = calStuffColors.accentSoft,
            onPrimaryContainer = calStuffColors.onAccentSoft,
            secondary = calStuffColors.highlight,
            background = calStuffColors.background,
            onBackground = calStuffColors.textPrimary,
            surface = calStuffColors.surface,
            onSurface = calStuffColors.textPrimary,
            surfaceVariant = calStuffColors.surfaceVariant,
            onSurfaceVariant = calStuffColors.textSecondary,
            surfaceTint = calStuffColors.accent,
            surfaceBright = calStuffColors.surface,
            surfaceDim = calStuffColors.background,
            surfaceContainerLowest = calStuffColors.background,
            surfaceContainerLow = calStuffColors.surface,
            surfaceContainer = calStuffColors.surface,
            surfaceContainerHigh = calStuffColors.surfaceVariant,
            surfaceContainerHighest = calStuffColors.surfaceVariant,
            outline = calStuffColors.divider,
            error = calStuffColors.error,
            onError = calStuffColors.onError,
            errorContainer = calStuffColors.errorSoft,
            onErrorContainer = calStuffColors.onErrorSoft,
            inverseSurface = calStuffColors.inverseSurface,
            inverseOnSurface = calStuffColors.onInverseSurface,
        )
    } else {
        lightColorScheme(
            primary = calStuffColors.accent,
            onPrimary = calStuffColors.onAccent,
            primaryContainer = calStuffColors.accentSoft,
            onPrimaryContainer = calStuffColors.onAccentSoft,
            secondary = calStuffColors.highlight,
            background = calStuffColors.background,
            onBackground = calStuffColors.textPrimary,
            surface = calStuffColors.surface,
            onSurface = calStuffColors.textPrimary,
            surfaceVariant = calStuffColors.surfaceVariant,
            onSurfaceVariant = calStuffColors.textSecondary,
            surfaceTint = calStuffColors.accent,
            surfaceBright = calStuffColors.surface,
            surfaceDim = calStuffColors.background,
            surfaceContainerLowest = calStuffColors.background,
            surfaceContainerLow = calStuffColors.surface,
            surfaceContainer = calStuffColors.surface,
            surfaceContainerHigh = calStuffColors.surfaceVariant,
            surfaceContainerHighest = calStuffColors.surfaceVariant,
            outline = calStuffColors.divider,
            error = calStuffColors.error,
            onError = calStuffColors.onError,
            errorContainer = calStuffColors.errorSoft,
            onErrorContainer = calStuffColors.onErrorSoft,
            inverseSurface = calStuffColors.inverseSurface,
            inverseOnSurface = calStuffColors.onInverseSurface,
        )
    }

    CompositionLocalProvider(LocalCalStuffColors provides calStuffColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}
