import Foundation

/// Match Android `BuildConfig.IS_STAGING` — set to `true` in staging Xcode configurations if you use build settings, or flip manually for QA.
enum LoginConfig {
    static let isStaging = false
    static let stagingPhoneDigits = "12345678910"
    static let stagingOtp = "999999"
}
