# `:shared` (Kotlin Multiplatform)

## What is KMP vs Android-only?

| Location | What lives here |
|----------|-----------------|
| **`:shared` (`commonMain`)** | Shared **contracts** and **pure Kotlin** types: `UserSessionRepository`, future models/use cases, `expect`/`actual` helpers. **No** Compose, **no** Firebase, **no** Android SDK. |
| **`:app` + `:data` (Android)** | **All current login behavior**: `LoginViewModel`, Firebase Phone/Google auth, `PhoneAuthRepository`, Hilt, OkHttp, DataStore/SharedPreferences. This is **not** duplicated in KMP yet. |
| **iOS (Swift/SwiftUI)** | UI only for now. **Wire** auth and session by calling into **`CalStuffShared`** (implement `UserSessionRepository` in `iosMain` or Swift) and your Firebase iOS SDK — same **rules** as Android, different **API surface**. |

So: **not** all code is KMP. Only the **shared module** is multiplatform; **login logic is still Android-first** until you move validation/rules into `commonMain` and add iOS auth wiring.

---

Shared **business contracts** and **commonMain** code are used by:

- **Android** (`:app` + `:data`) — Jetpack Compose UI, Hilt, Android SDKs.
- **iOS** — Swift / SwiftUI app embedding the **`CalStuffShared`** framework.

## Layout

| Source set    | Purpose |
|---------------|---------|
| `commonMain` | Interfaces, models, use cases, expect/actual APIs. |
| `androidMain` | Optional Android-specific `actual` implementations. |
| `iosMain`     | Optional iOS-specific `actual` implementations. |

`UserSessionRepository` lives in `commonMain`; Android implements it in `:data`. On iOS, implement the same interface (e.g. `UserDefaults` / Keychain) in Kotlin `iosMain` or bridge from Swift.

## SwiftUI login (design parity)

Swift files that **match the Compose login layout** (colors, spacing, phone/Google/OTP flow) live under **`ios/CalStuffLogin/`**. Add them to your Xcode target; see **`ios/README.md`**.

## Build iOS framework (for Xcode)

From the project root:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Framework output is under `shared/build/bin/iosSimulatorArm64/debugFramework/` (exact path may vary by Kotlin/Gradle version). Add the **CalStuffShared.framework** to your Xcode target (Embed & Sign).

For **device** builds:

```bash
./gradlew :shared:linkDebugFrameworkIosArm64
```

**Requires Xcode + Command Line Tools** for linking.

## Next steps

- Move validation and session **rules** from Android-only code into `commonMain` where it makes sense.
- Add `kotlinx-serialization` in `commonMain` for DTOs shared with Ktor clients.
- Use **expect/actual** for secure storage, analytics, or date/time on each platform.
