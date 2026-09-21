import Foundation

/// Match Android `BuildConfig.IS_STAGING` — set `true` for QA builds.
/// TODO: Android gets this per-build from a Gradle product flavor (staging/prod). There's no
/// equivalent Xcode scheme/configuration split yet, so this is a single hardcoded switch for now.
///
/// Set to `false`: the staging test number below is registered in Firebase Console
/// (Authentication → Phone → "Phone numbers for testing") — routing it through the *real*
/// verifyPhoneNumber/signIn path still requires no real SMS and returns the same fixed OTP, but
/// now creates a genuine signed-in Firebase user, which Firestore reads/writes require. Flip
/// back to `true` only if the Console test number isn't set up yet (see docs/FIREBASE_SETUP.md §5).
enum LoginConfig {
    static let isStaging = false
    /// Digits only — same as Firebase test phone +91 9999999999
    static let stagingPhoneDigits = "9999999999"
    static let stagingOtp = "999999"
}
