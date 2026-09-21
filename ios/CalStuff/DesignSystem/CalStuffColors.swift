import SwiftUI

/// Mirrors Android's `CalStuffColors` (app/.../ui/theme/Color.kt + Theme.kt) token-for-token so
/// both platforms render identically. Read via `\.calStuffColors` in the environment (see
/// Theme.swift) — never construct a `Color(hex:)` literal directly in feature code.
struct CalStuffColors {
    let background: Color
    let surface: Color
    let surfaceVariant: Color
    let textPrimary: Color
    let textSecondary: Color
    let divider: Color

    let accent: Color
    let onAccent: Color
    let accentSoft: Color
    let onAccentSoft: Color

    let success: Color
    let highlight: Color
    /// Content color for anything painted with a solid `highlight` fill — always a fixed dark
    /// ink, since the bright yellow needs dark contrast in both themes (unlike `textPrimary`).
    let onHighlight: Color
    let info: Color

    let error: Color
    let onError: Color
    let errorSoft: Color
    let onErrorSoft: Color

    /// Deliberately-inverted card style (e.g. the black calendar widget) — same in both themes.
    let inverseSurface: Color
    let onInverseSurface: Color
    let onInverseSurfaceVariant: Color

    let gradientStart: Color
    let gradientEnd: Color
    let onGradient: Color
    let onGradientMuted: Color
}

/// Brand colors — identical in both themes; a brand identity shouldn't flip with the mode.
private enum Brand {
    static let accent = Color(hex: 0xFF6F3C)
    static let accentBright = Color(hex: 0xFF9A56)
    static let highlight = Color(hex: 0xFFD643)
    static let success = Color(hex: 0x66BB6A)
    static let info = Color(hex: 0x42A5F5)
}

private enum LightPalette {
    static let background = Color(hex: 0xF3F1EB)
    static let surface = Color(hex: 0xFFFFFF)
    static let surfaceVariant = Color(hex: 0xF5F5F5)
    static let textPrimary = Color(hex: 0x1C1B1F)
    static let textSecondary = Color(hex: 0x9E9E9E)
    static let divider = Color(hex: 0xEEEEEE)
    static let accentSoft = Color(hex: 0xFFE0CC)
    static let onAccentSoft = Brand.accent
    static let error = Color(hex: 0xF85B4E)
    static let errorSoft = Color(hex: 0xFDE7E5)
    static let onErrorSoft = error
}

/// Dark theme surfaces and text — warm charcoal, not pure black, so it still reads as CalStuff.
private enum DarkPalette {
    static let background = Color(hex: 0x232120)
    static let surface = Color(hex: 0x2E2C2A)
    static let surfaceVariant = Color(hex: 0x3A3733)
    static let textPrimary = Color(hex: 0xF5F3EF)
    static let textSecondary = Color(hex: 0xB7B2AA)
    static let divider = Color(hex: 0x46443D)
    static let accentSoft = Color(hex: 0x4A3626)
    static let onAccentSoft = Color(hex: 0xFF9A56)
    static let error = Color(hex: 0xFF7A6B)
    static let errorSoft = Color(hex: 0x4A2A26)
    static let onErrorSoft = error
}

extension CalStuffColors {
    static let light = CalStuffColors(
        background: LightPalette.background,
        surface: LightPalette.surface,
        surfaceVariant: LightPalette.surfaceVariant,
        textPrimary: LightPalette.textPrimary,
        textSecondary: LightPalette.textSecondary,
        divider: LightPalette.divider,
        accent: Brand.accent,
        onAccent: .white,
        accentSoft: LightPalette.accentSoft,
        onAccentSoft: LightPalette.onAccentSoft,
        success: Brand.success,
        highlight: Brand.highlight,
        onHighlight: .black,
        info: Brand.info,
        error: LightPalette.error,
        onError: .white,
        errorSoft: LightPalette.errorSoft,
        onErrorSoft: LightPalette.onErrorSoft,
        inverseSurface: .black,
        onInverseSurface: .white,
        onInverseSurfaceVariant: Brand.highlight,
        gradientStart: Brand.accentBright,
        gradientEnd: Brand.accent,
        onGradient: .white,
        onGradientMuted: Color.white.opacity(0.85)
    )

    static let dark = CalStuffColors(
        background: DarkPalette.background,
        surface: DarkPalette.surface,
        surfaceVariant: DarkPalette.surfaceVariant,
        textPrimary: DarkPalette.textPrimary,
        textSecondary: DarkPalette.textSecondary,
        divider: DarkPalette.divider,
        accent: Brand.accent,
        onAccent: .white,
        accentSoft: DarkPalette.accentSoft,
        onAccentSoft: DarkPalette.onAccentSoft,
        success: Brand.success,
        highlight: Brand.highlight,
        onHighlight: .black,
        info: Brand.info,
        error: DarkPalette.error,
        onError: .white,
        errorSoft: DarkPalette.errorSoft,
        onErrorSoft: DarkPalette.onErrorSoft,
        inverseSurface: .black,
        onInverseSurface: .white,
        onInverseSurfaceVariant: Brand.highlight,
        gradientStart: Brand.accentBright,
        gradientEnd: Brand.accent,
        onGradient: .white,
        onGradientMuted: Color.white.opacity(0.85)
    )
}
