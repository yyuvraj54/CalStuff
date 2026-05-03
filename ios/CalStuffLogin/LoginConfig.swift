import Foundation

/// Match Android `BuildConfig.IS_STAGING` — set `true` for QA builds.
enum LoginConfig {
    static let isStaging = false
    /// Digits only — same as Firebase test phone +91 9999999999
    static let stagingPhoneDigits = "9999999999"
    static let stagingOtp = "999999"
}
