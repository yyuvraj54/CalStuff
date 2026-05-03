# Firebase setup — Android + iOS (CalStuff)

Use **one Firebase project** for both apps (current Android config: **`calstuff-99d09`**) so Authentication, Analytics, and future services share the same users and rules.

---

## 1. Firebase Console (one-time)

1. Open [Firebase Console](https://console.firebase.google.com/) → your project (e.g. **`calstuff-99d09`**).
2. **Authentication** → Sign-in method:
   - Enable **Phone**
   - Enable **Google** (support email, Web SDK client is created automatically)
3. For **Phone auth on iOS**, you may need **APNs** (Apple Push Notification service) for production silent verification — Firebase documents this under *Phone Auth → iOS*. For early development, reCAPTCHA fallback often works in Simulator/device.

---

## 2. Android (already wired)

| Item | Location |
|------|----------|
| Gradle plugin | Root `build.gradle.kts`: `google-services` `apply false`; **`:app`** applies the plugin |
| Config file | `app/google-services.json` (from Console → Project settings → Your apps → Android). **Replace** the file in the repo when Firebase gives you a new download — it must match the project you use in Console. |
| Package name | `com.dusht.calstuff` (must match the registered Android app) |
| Initialization | `MyApplication` → `FirebaseApp.initializeApp(this)` |
| Dependencies | `app/build.gradle.kts`: `firebase-bom` + `firebase-auth` |

**After changing** `google-services.json`, sync Gradle and rebuild.

### `google-services.json` from Downloads vs repo

- The file under **`app/google-services.json`** is what Gradle uses. Your **`Downloads/google-services.json`** should be **copied over** that path (or drag-replace in Android Studio) whenever Firebase gives you an updated config.
- If the new file has **`oauth_client": []`** (empty), **Google Sign-In** is not fully configured yet. In Firebase: add your app’s **SHA-1** (Project settings → Your apps → Android), enable **Google** under Authentication, then **re-download** `google-services.json`. The Google Services plugin can then emit **`default_web_client_id`**, which the app uses when `strings.xml` → `web_client_id` is left empty.
- You can also paste the **Web client ID** manually into `app/src/main/res/values/strings.xml` → `web_client_id` (see comment in that file).

**SHA-1 / SHA-256** (Play + Google Sign-In): Project settings → Your apps → Android app → *Add fingerprint* (debug + release keystores).

---

## 3. iOS (Xcode project on Desktop)

### 3.1 Register the iOS app in Firebase

1. Firebase Console → **Project settings** → **Your apps** → **Add app** → **iOS**.
2. **Bundle ID**: use a stable id, e.g. **`com.dusht.calstuff.ios`** (must match Xcode **Signing & Capabilities**).
3. Download **`GoogleService-Info.plist`** from Firebase and place it in the app sources folder:
   - **Desktop Xcode project:** `ios/CalStuff/CalStuff/GoogleService-Info.plist` (same folder as `CalStuffApp.swift`). The project uses a synchronized folder, so the file is included automatically.
   - **Repo copy:** `ios/CalStuffLogin/GoogleService-Info.plist` (kept in sync for reference).
4. If your plist is **missing** `CLIENT_ID` and **`REVERSED_CLIENT_ID`**, re-download from **Firebase Console → Project settings → Your apps → iOS app** (full plist). You need **`REVERSED_CLIENT_ID`** for Google Sign-In’s **URL scheme** in Xcode → target → **Info** → **URL Types**.
5. Remove or ignore **`GoogleService-Info.plist.example`** once the real file is present (avoid shipping two plists with conflicting names in the same target).

### 3.2 Swift Package Manager (already added in `CalStuff.xcodeproj`)

Per [Firebase iOS setup](https://firebase.google.com/docs/ios/setup), the project uses **File → Add Package Dependencies** with:

| Repository | Products linked to the **CalStuff** app target |
|------------|-----------------------------------------------|
| `https://github.com/firebase/firebase-ios-sdk` | **FirebaseCore**, **FirebaseAuth**, **FirebaseAnalytics** |
| `https://github.com/google/GoogleSignIn-iOS` | **GoogleSignIn** |

- **Version:** *Up to Next Major* from **11.0.0** (you can change in Xcode → Project → Package Dependencies).
- **Analytics:** **FirebaseAnalytics** is included so default Analytics collection runs after `FirebaseApp.configure()`. If you want Analytics **without** IDFA-related APIs, remove **FirebaseAnalytics** in Xcode and add **FirebaseAnalyticsWithoutAdId** instead (same package URL).

Open the project in Xcode once so packages **resolve** ( **File → Packages → Resolve Package Versions** ).

### 3.3 URL scheme (Google Sign-In)

1. Open the downloaded **`GoogleService-Info.plist`** and copy the value of **`REVERSED_CLIENT_ID`** (looks like `com.googleusercontent.apps.xxxxx`).
2. Xcode → target **CalStuff** → **Info** → **URL Types** → **+**:
   - **URL Schemes**: paste **`REVERSED_CLIENT_ID`** exactly.
3. This allows the Google sign-in redirect back into your app.

### 3.4 Code entry points

| File | Role |
|------|------|
| `CalStuffApp.swift` | `FirebaseApp.configure()`, `onOpenURL` → `FirebaseAuthService.handleGoogleURL` |
| `FirebaseAuthService.swift` | Phone verification, SMS sign-in, Google sign-in |
| `LoginViewModel.swift` | Calls `FirebaseAuthService` for Continue / Verify / Google |

### 3.5 Bundle ID in the repo

Set **Product Bundle Identifier** to match Firebase (e.g. `com.dusht.calstuff.ios`), not `None.CalStuff`.

---

## 4. Same project, two apps — checklist

| Check | Android | iOS |
|-------|---------|-----|
| Same Firebase **project** | ✓ (e.g. `calstuff-99d09`) | ✓ |
| Auth providers enabled | Phone + Google | Same (Console is shared) |
| Config file | `google-services.json` | `GoogleService-Info.plist` |
| Package / bundle id | `com.dusht.calstuff` | e.g. `com.dusht.calstuff.ios` |
| OAuth / SHA | SHA-1/256 in Firebase | **REVERSED_CLIENT_ID** URL scheme |

---

## 5. Phone test number (+91 9999999999 / OTP 999999)

**Firebase Console:** Authentication → Phone → add test number **+91 9999999999** with code **999999**.

**Android**

- **`PhoneAuthRepository`:** `STAGING_TEST_PHONE_DIGITS` = **`9999999999`** (digits only), `STAGING_TEST_OTP` = **`999999`**. Matches the in-app staging shortcut and the Console test number.
- **`staging`** flavor → `BuildConfig.IS_STAGING = true` → mock SMS path for that exact digit string.
- **`prod`** flavor (default) → real Firebase Phone Auth; use the same test number in the field — Firebase issues OTP **999999** per Console.
- **E.164:** 10-digit numbers starting with **6–9** are sent to Firebase as **`+91`** (India national format).

**iOS:** `LoginConfig.stagingPhoneDigits` / `stagingOtp` match the same values.

---

## 6. Troubleshooting

| Symptom | What to check |
|---------|----------------|
| Android: “default FirebaseApp is not initialized” | `google-services.json` in `app/`, `google-services` plugin on `:app`, `FirebaseApp.initializeApp` |
| iOS: configure fails / missing options | `GoogleService-Info.plist` in app target, bundle id matches Console |
| Google Sign-In fails on iOS | URL scheme = `REVERSED_CLIENT_ID`, same bundle id as Firebase iOS app |
| Phone auth fails on iOS | APNs / reCAPTCHA per Firebase iOS phone docs; try real device |
| Gradle: `google-services` not found | Root `build.gradle.kts` includes `alias(libs.plugins.google.gms.google.services) apply false` |

---

## 7. Optional next steps

- Enable **Firebase Analytics** on both platforms (add dependency / SPM product).
- **App Check** for API abuse protection.
- **Firestore / RTDB**: add SDKs in the same way; keep security rules in Console.

For cross-platform architecture (KMP, mocks, prod API), see [`CROSS_PLATFORM_ARCHITECTURE.md`](CROSS_PLATFORM_ARCHITECTURE.md).
