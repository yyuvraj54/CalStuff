# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

CalStuff is a Kotlin Multiplatform (KMP) project targeting Android (Jetpack Compose) and iOS (SwiftUI). The Android side is the primary development surface today; iOS has login UI parity via Swift files under `ios/CalStuffLogin/`.

## Build commands

```bash
# Android debug build (default prod flavor)
./gradlew assembleProdDebug

# Android staging build
./gradlew assembleStagingDebug

# Run Android unit tests
./gradlew testProdDebugUnitTest

# Build iOS simulator framework from shared module
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Build iOS device framework
./gradlew :shared:linkDebugFrameworkIosArm64

# Clean
./gradlew clean
```

## Module structure

- **`:app`** — Compose UI, navigation, feature screens, Hilt ViewModels. Package: `com.dusht.calstuff`
- **`:shared`** — KMP module (`commonMain`/`androidMain`/`iosMain`). Shared contracts, models, use cases. Produces `CalStuffShared` framework for iOS.
- **`:data`** — Android-only implementations of `:shared` interfaces. Hilt `@Module`/`@Binds` bindings, OkHttp, Firebase, preferences.
- **`:core-logging`** — Timber setup, `AppLogger` facade with tags: API, NAV, LIFECYCLE, APP.

## Architecture

**MVI pattern**: State (immutable data class) → UI, Events → ViewModel, Effects (one-shot via Channel) → navigation/snackbars.

Base classes in `com.dusht.calstuff.utils.base`: `BaseViewModel`, `ViewState`, `ViewEvent`, `ViewEffect`.

**Dependency injection**: Dagger Hilt. Bindings live in `:data` module (`DataBindsModule`/`DataProvidesModule`, `@InstallIn(SingletonComponent::class)`).

**Navigation**: Type-safe routes via `kotlinx.serialization` in `AppRoute` (sealed interface). `AppNavController` wraps `NavHostController`. Add screens by adding a `@Serializable` route to `AppRoute` and registering in `AppNavGraph`.

**Build flavors**: `staging` (IS_STAGING=true) and `prod` (IS_STAGING=false, default). compileSdk/targetSdk=36, minSdk=24.

## KMP conventions

- New shared logic goes in `shared/src/commonMain/kotlin/`
- Use `expect`/`actual` only when platform APIs differ
- No Android SDK or Compose imports in `commonMain`
- Interfaces defined in `:shared`, implemented in `:data` (Android) or Swift/`iosMain` (iOS)

## Key dependencies

- Kotlin serialization for navigation routes and DTOs
- Firebase Auth (Phone + Google sign-in) via BOM
- Credential Manager for Google sign-in
- Coil 3 for image loading
- Ktor planned for shared networking (not yet wired)
- Version catalog: `gradle/libs.versions.toml`

## Documentation

Detailed architecture docs live in `docs/`:
- `ARCHITECTURE.md` — Android MVI, modules, Hilt, navigation, logging
- `docs/CROSS_PLATFORM_ARCHITECTURE.md` — KMP strategy, prod vs mock networking, roadmap
- `docs/FIREBASE_SETUP.md` — Firebase config for Android + iOS
- `shared/README.md` — KMP module details and iOS framework build
- `ios/README.md` — SwiftUI login files and Xcode setup
