# iOS (SwiftUI) — CalStuff

## Firebase (Phone + Google)

The Desktop Xcode app under `~/Desktop/ios/CalStuff/` includes **Swift Package Manager** dependencies for **Firebase** and **Google Sign-In**, `FirebaseApp.configure()`, and auth wiring.

**Setup checklist (Console, plist, URL scheme):** **[`docs/FIREBASE_SETUP.md`](../docs/FIREBASE_SETUP.md)**

Copy **`GoogleService-Info.plist.example`** to **`GoogleService-Info.plist`** in the Xcode app target after you register the iOS app in Firebase (bundle id e.g. `com.dusht.calstuff.ios`).

## Adding the SwiftUI login to Xcode

1. Create an iOS App project in Xcode (SwiftUI lifecycle) or open your existing app.
2. Add Swift packages: **Firebase iOS SDK** (`FirebaseCore`, `FirebaseAuth`, `FirebaseAnalytics`) and **GoogleSignIn-iOS** (`GoogleSignIn`) — see `docs/FIREBASE_SETUP.md`.
3. Drag the **`CalStuffLogin`** folder (`ios/CalStuffLogin/`) into the project navigator (copy if needed, create groups).
4. Embed **`CalStuffShared.framework`** from Gradle (see `../shared/README.md`).
5. Add **`login_bg`** / **`google_btn_icon`** to **Assets.xcassets** (or use placeholders); see repo `ios/CalStuffLogin/Assets.xcassets`.

## Staging hint (matches Android `BuildConfig.IS_STAGING`)

In **`LoginConfig.swift`**, set `isStaging` to `true` for internal builds so the staging phone/OTP hint appears like on Android.

## Post-login flow (Desktop Xcode app)

After phone/Google sign-in, **`RootView`** calls **`UserProfileGateService.fetchProfileCompleteness()`**. If `isProfileComplete` is **`false`**, **`ProfileOnboardingView`** runs (name, age, gender, height, weight, activity). If **`true`**, **`HomeView`** shows.

- **Mock:** `Services/UserProfileGateService.swift` returns `isProfileComplete: false` so the onboarding form appears. Set to `true` there to test skipping straight to home.
- **Reusable UI:** `Common/` — `CalStuffDesignSystem`, `OnboardingShellLayout`, `StepPageIndicator`, `PrimaryStickyButton`.

The canonical Swift tree lives under **`~/Desktop/ios/CalStuff/CalStuff/`** (not only `ios/CalStuffLogin/`).

## Shared logic vs platform

- **UI** is mirrored in SwiftUI (`LoginView`).
- **Auth** in this repo uses **Firebase iOS SDK** in `FirebaseAuthService.swift`; session persistence can later align with **`CalStuffShared`** (`UserSessionRepository`).
