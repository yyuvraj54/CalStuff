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

| Source set    | On disk (under `shared/src/`) | Purpose |
|---------------|------------------------------|---------|
| `commonMain`  | **`commonMain/kotlin/...`**   | Interfaces, models, use cases, `expect` declarations. |
| `androidMain` | **`androidMain/kotlin/...`** | Optional Android-specific `actual` implementations. |
| `iosMain`     | **`iosMain/kotlin/...`**     | Optional iOS-specific `actual` implementations. |

There are **no** Gradle source sets named `platform`, `platform.android`, or `platform.ios` in this project. If you created folders with those names, they are **not** wired into KMP unless you add custom `sourceSets { }` blocks in `shared/build.gradle.kts` — the standard names above are what Kotlin uses.

### What is in those folders today?

Minimal **expect/actual** wiring to prove the module compiles for Android and iOS:

| File | Role |
|------|------|
| `commonMain/.../Platform.kt` | `expect fun calStuffPlatformName(): String` |
| `androidMain/.../Platform.android.kt` | `actual` → `"Android"` |
| `iosMain/.../Platform.ios.kt` | `actual` → `"iOS"` |

Most **product** code still lives in **`:app` / `:data` (Android)** and **Swift (iOS)**. You add more under `commonMain` / `androidMain` / `iosMain` only when you need **shared Kotlin** that differs per platform (e.g. `expect fun readMockJson(path: String): String` with `actual` reading Assets vs Bundle).

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

**Firebase (Android + iOS):** [`docs/FIREBASE_SETUP.md`](../docs/FIREBASE_SETUP.md).

**Broader plan (networking mocks, prod backend, Watch, documentation index):** [`docs/CROSS_PLATFORM_ARCHITECTURE.md`](../docs/CROSS_PLATFORM_ARCHITECTURE.md).

## Next steps

- Move validation and session **rules** from Android-only code into `commonMain` where it makes sense.
- Add `kotlinx-serialization` in `commonMain` for DTOs shared with Ktor clients.
- Use **expect/actual** for secure storage, analytics, or date/time on each platform.
