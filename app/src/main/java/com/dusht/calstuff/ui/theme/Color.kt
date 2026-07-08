package com.dusht.calstuff.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw color values for the design system — grouped by palette, one source of truth per role.
 *
 * Nothing outside this file should reference a `Color(0xFF...)` literal. Screens read colors
 * through [CalStuffColors] (`MaterialTheme.calStuffColors.xxx`, see Theme.kt) so every surface
 * follows the active light/dark mode automatically.
 */

/** Brand colors — identical in both themes; a brand identity shouldn't flip with the mode. */
private object Brand {
    val accent = Color(0xFFFF6F3C)       // primary orange — icons, CTAs, buttons
    val accentBright = Color(0xFFFF9A56) // lighter orange, e.g. gradient tops
    val highlight = Color(0xFFFFD643)    // signature yellow — progress rings, streak chart
    val success = Color(0xFF66BB6A)      // calorie-under-goal green
    val info = Color(0xFF42A5F5)         // informational blue — e.g. "underweight" BMI zone
}

/** Light theme surfaces and text. */
private object LightPalette {
    val background = Color(0xFFF3F1EB)
    val surface = Color(0xFFFFFFFF)
    val surfaceVariant = Color(0xFFF5F5F5)
    val textPrimary = Color(0xFF1C1B1F)
    val textSecondary = Color(0xFF9E9E9E)
    val divider = Color(0xFFEEEEEE)
    val accentSoft = Color(0xFFFFE0CC)
    val onAccentSoft = Brand.accent
    val error = Color(0xFFF85B4E)
    val errorSoft = Color(0xFFFDE7E5)
    val onErrorSoft = error
}

/** Dark theme surfaces and text — warm charcoal, not pure black, so it still reads as CalStuff. */
private object DarkPalette {
    val background = Color(0xFF232120)
    val surface = Color(0xFF2E2C2A)
    val surfaceVariant = Color(0xFF3A3733)
    val textPrimary = Color(0xFFF5F3EF)
    val textSecondary = Color(0xFFB7B2AA)
    val divider = Color(0xFF46443D)
    val accentSoft = Color(0xFF4A3626)
    val onAccentSoft = Color(0xFFFF9A56)
    val error = Color(0xFFFF7A6B)
    val errorSoft = Color(0xFF4A2A26)
    val onErrorSoft = error
}

/** The full semantic token set — see [CalStuffColors] in Theme.kt for the field docs. */
internal val LightCalStuffColors = CalStuffColors(
    background = LightPalette.background,
    surface = LightPalette.surface,
    surfaceVariant = LightPalette.surfaceVariant,
    textPrimary = LightPalette.textPrimary,
    textSecondary = LightPalette.textSecondary,
    divider = LightPalette.divider,
    accent = Brand.accent,
    onAccent = Color.White,
    accentSoft = LightPalette.accentSoft,
    onAccentSoft = LightPalette.onAccentSoft,
    success = Brand.success,
    highlight = Brand.highlight,
    onHighlight = Color.Black,
    info = Brand.info,
    error = LightPalette.error,
    onError = Color.White,
    errorSoft = LightPalette.errorSoft,
    onErrorSoft = LightPalette.onErrorSoft,
    inverseSurface = Color.Black,
    onInverseSurface = Color.White,
    onInverseSurfaceVariant = Brand.highlight,
    gradientStart = Brand.accentBright,
    gradientEnd = Brand.accent,
    onGradient = Color.White,
    onGradientMuted = Color.White.copy(alpha = 0.85f),
)

internal val DarkCalStuffColors = CalStuffColors(
    background = DarkPalette.background,
    surface = DarkPalette.surface,
    surfaceVariant = DarkPalette.surfaceVariant,
    textPrimary = DarkPalette.textPrimary,
    textSecondary = DarkPalette.textSecondary,
    divider = DarkPalette.divider,
    accent = Brand.accent,
    onAccent = Color.White,
    accentSoft = DarkPalette.accentSoft,
    onAccentSoft = DarkPalette.onAccentSoft,
    success = Brand.success,
    highlight = Brand.highlight,
    onHighlight = Color.Black,
    info = Brand.info,
    error = DarkPalette.error,
    onError = Color.White,
    errorSoft = DarkPalette.errorSoft,
    onErrorSoft = DarkPalette.onErrorSoft,
    inverseSurface = Color.Black,
    onInverseSurface = Color.White,
    onInverseSurfaceVariant = Brand.highlight,
    gradientStart = Brand.accentBright,
    gradientEnd = Brand.accent,
    onGradient = Color.White,
    onGradientMuted = Color.White.copy(alpha = 0.85f),
)

// ── Legacy Compose-template colors (unused; kept only so any stale references don't break) ─
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
