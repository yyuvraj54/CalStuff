# iOS (SwiftUI) — CalStuff

## Adding the SwiftUI login to Xcode

1. Create an iOS App project in Xcode (SwiftUI lifecycle) or open your existing app.
2. Drag the **`CalStuffLogin`** folder (`ios/CalStuffLogin/`) into the project navigator (copy if needed, create groups).
3. Embed **`CalStuffShared.framework`** from Gradle (see `../shared/README.md`).
4. Add **`login_bg`** to **Assets.xcassets** if you want the same hero image as Android (name it `login_bg`), or the view uses a soft gradient placeholder.
5. Optionally add **`google_btn_icon`** to Assets to match Android; otherwise a system icon is used.

## Staging hint (matches Android `BuildConfig.IS_STAGING`)

In **`LoginConfig.swift`**, set `isStaging` to `true` for internal builds so the staging phone/OTP hint appears like on Android.

## Same logic as Android?

- **UI** is mirrored in SwiftUI (`LoginView`).
- **Auth** (Firebase Phone, Google) must be implemented with **Firebase iOS SDK** + your bridge to **`CalStuffShared`** session APIs — not automatic from these files.
