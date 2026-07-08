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

# Run a single test class/method (any module)
./gradlew :app:testProdDebugUnitTest --tests "com.dusht.calstuff.ExampleUnitTest"
./gradlew :data:testDebugUnitTest --tests "com.dusht.data.SomeTest.someMethod"

# Build iOS simulator framework from shared module
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Build iOS device framework
./gradlew :shared:linkDebugFrameworkIosArm64

# Clean
./gradlew clean
```

## Module structure

- **`:app`** — Compose UI, navigation, feature screens, Hilt ViewModels. Package: `com.dusht.calstuff`
- **`:shared`** — KMP module (`commonMain`/`androidMain`/`iosMain`). Package: `com.dusht.shared`. Produces `CalStuffShared` framework for iOS. `commonMain` already holds real domain contracts, not just the `expect`/`actual` platform-name stub: `UserSessionRepository`, `DisplayNameStore`, `UserProfileRepository`, `NutritionRepository`, `StreakRepository`, `ProfileGateRepository`, plus their models (`UserProfile`, `NutritionModels`, `StreakData`).
- **`:data`** — Android-only implementations of the `:shared` interfaces above. Package: `com.dusht.data`. Hilt bindings split across `DataBindsModule` (`@Binds`) and `DataProvidesModule` (`@Provides`) in `data/di/DataModule.kt`. Backed by **Room** (`CalStuffDatabase`, local cache/single source of truth for the UI — see DAOs under `data/local/dao`), **Firestore** + **Firebase Auth** (remote user data), and a shared `OkHttpClient` whose logging interceptor routes through `AppLogger.api` (body logging only when `BuildConfig.DEBUG`).
- **`:core-logging`** — Timber setup, `AppLogger` facade with tags: API, NAV, LIFECYCLE, APP.

## Architecture

**MVI pattern**: State (immutable data class) → UI, Events → ViewModel, Effects (one-shot via Channel) → navigation/snackbars.

Base classes in `com.dusht.calstuff.utils.base`: `BaseViewModel`, `ViewState`, `ViewEvent`, `ViewEffect`.

**Dependency injection**: Dagger Hilt. Bindings live in `:data` module (`DataBindsModule`/`DataProvidesModule`, `@InstallIn(SingletonComponent::class)`).

**Navigation**: Type-safe routes via `kotlinx.serialization` in `AppRoute` (sealed interface). `AppNavController` wraps `NavHostController`. Add screens by adding a `@Serializable` route to `AppRoute` and registering in `AppNavGraph`.

**Build flavors**: `staging` (IS_STAGING=true) and `prod` (IS_STAGING=false, default). compileSdk/targetSdk=36, minSdk=24.

**Feature screens** (`app/src/main/java/com/dusht/calstuff/ui/`):
- `screens/navscreen/{home,meals,logs,profile,chat}` — bottom-nav tab destinations (see `BottomNavDestination` in `AppRoute.kt`).
- `screens/onboarding`, `screens/addmeal` — non-tab flows pushed on top of the nav graph.
- `components/{nutrition,streak,calendar,bmi,logs,weekly,widgetgrid}` — reusable widgets composed into the home screen's widget grid; `model/widget` holds their shared data models.

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
