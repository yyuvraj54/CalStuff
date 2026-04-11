import SwiftUI

/// Matches Compose `LoginScreen` / `CalAuraTextField` colors for visual parity with Android.
enum LoginDesignTokens {
    static let background = Color.white
    static let title = Color.black
    static let description = Color.gray
    static let stagingAccent = Color(red: 0.4, green: 0.26, blue: 0.65) // primary-ish; tweak to match Material primary
    static let auraFieldFill = Color(red: 0.96, green: 0.97, blue: 0.98) // #F5F7FA
    static let auraBorder = Color(red: 0.88, green: 0.89, blue: 0.92) // #E0E4EB
    static let auraBorderFocused = Color(red: 0.72, green: 0.77, blue: 0.85) // #B8C4D9
    static let dividerLine = Color(red: 0.88, green: 0.89, blue: 0.92)
    static let orLabel = Color(red: 0.45, green: 0.45, blue: 0.45)
    static let continueButton = Color(red: 0.36, green: 0.42, blue: 0.75) // #5C6BC0
    static let verifyButton = Color(red: 0.15, green: 0.65, blue: 0.60) // #26A69A
    static let googleButton = Color(red: 1.0, green: 0.42, blue: 0.42) // #FF6B6B
    static let otpHint = Color(red: 0.45, green: 0.45, blue: 0.45)
    static let fieldText = Color(red: 0.12, green: 0.14, blue: 0.18)
    static let fieldPlaceholder = Color(red: 0.55, green: 0.57, blue: 0.62)
    static let fieldCursor = Color(red: 0.25, green: 0.35, blue: 0.75)
}

extension Color {
    init(hex: UInt32, alpha: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 255,
            green: Double((hex >> 8) & 0xff) / 255,
            blue: Double(hex & 0xff) / 255,
            opacity: alpha
        )
    }
}
