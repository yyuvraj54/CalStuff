# CalStuff architecture

This document describes how the Android app is structured today and how it is intended to scale with Kotlin Multiplatform (KMP) and an iOS (SwiftUI) client.

**Cross-platform strategy, prod vs mock networking, roadmap, and iOS learning notes:** see **[`docs/CROSS_PLATFORM_ARCHITECTURE.md`](docs/CROSS_PLATFORM_ARCHITECTURE.md)**.

## Goals

- **MVI + Jetpack Compose** on Android: single-direction data flow (State → UI, Events → ViewModel, one-shot Effects for navigation/snackbars).
- **Modular Gradle projects** so networking, logging, and domain rules stay testable and swappable.
- **Dagger Hilt** for a single app-wide graph (`SingletonComponent`) with `@InstallIn` modules in the `data` layer.
- **Debug observability**: Timber with consistent tags for **API**, **NAV**, **LIFECYCLE**, and **APP** events.
- **Navigation** that is type-safe (`kotlinx.serialization` routes), centralized in `AppNavController`, and logged on every destination change.
- **No leaks / good performance**: ViewModels own coroutines; Compose collects effects in `LaunchedEffect` tied to lifecycle; channels for one-shot effects; no long-lived references to UI.

## Module map

| Module        | Role |
|---------------|------|
| `:app`        | Compose UI, navigation host, feature screens, Hilt `@AndroidEntryPoint` activities, `@HiltViewModel` implementations. |
| `:shared`     | **Kotlin Multiplatform**: `commonMain` business contracts (e.g. `UserSessionRepository`), shared models/use cases, `expect`/`actual` helpers. Targets: **Android** (via KMP Android target) and **iOS** (`iosArm64`, `iosSimulatorArm64`) producing **`CalStuffShared`** framework for Swift. |
| `:data`       | Android-only implementations (prefs, OkHttp, Firebase), Hilt `@Module` / `@Binds` implementing `:shared` interfaces. |
| `:core-logging` | Timber setup (`TimberInitializer`), `AppLogger` façade for structured debug lines. |

### Kotlin Multiplatform (current)

- Add new **shared logic** under `shared/src/commonMain/kotlin`.
- Use **`androidMain`** / **`iosMain`** only when platform APIs differ (`expect`/`actual`).
- **iOS**: build the framework (`shared/README.md`), embed **CalStuffShared** in Xcode; UI stays **Swift/SwiftUI**.

## Layering

```
UI (Compose)
    → ViewModel (MVI: state / event / effect)
        → `:shared` interfaces (repository, use case)
            → data implementations (prefs, network, DB)
```

- **State**: immutable `data class` implementing `ViewState`.
- **Event**: user actions (`ViewEvent`).
- **Effect**: one-time side effects (`ViewEffect`) delivered through a `Channel` and collected in Compose with `LaunchedEffect`.

Shared bases live under `com.dusht.calstuff.utils.base` (`BaseViewModel`, optional `BaseActivity` for classic Activity-hosted MVI).

## Dependency injection (Hilt)

- `@HiltAndroidApp` on `MyApplication`.
- `@AndroidEntryPoint` on `CalStuffMainActivity`.
- `@HiltViewModel` on feature ViewModels; obtain them with `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()`.
- Repository bindings: `DataBindsModule` / `DataProvidesModule` in `:data` (`@InstallIn(SingletonComponent::class)`).

Add new bindings by:

1. Declaring an interface in `:shared` (`commonMain`).
2. Implementing it in `:data` with `@Singleton` / constructor `@Inject` as appropriate.
3. Adding `@Binds` or `@Provides` in `DataModule` (or a new `@InstallIn` module in `:data`).

## Logging (debug)

- `TimberInitializer.init(BuildConfig.DEBUG)` runs in `MyApplication`.
- **`AppLogger.api`**: OkHttp pipeline (request line, response code, duration; body via `HttpLoggingInterceptor` **only in debug**).
- **`AppLogger.navigation`**: `NavController.OnDestinationChangedListener` in `AppNavGraph`.
- **`AppLogger.lifecycle`**: key activity callbacks on `CalStuffMainActivity`.
- **`AppLogger.app`**: product/analytics-style events (sign-in, logout, chat send, etc.).

Release builds use a small `Timber.Tree` that forwards only **WARN** and above to the system log; extend with Crashlytics if needed.

## Navigation

- Routes are `kotlinx.serialization` types in `AppRoute` (sealed interface).
- `AppNavController` wraps `NavHostController` with helpers (`navigateToBottomDestination`, `navigateAndClearBackStack`, `navigateUp`).
- `LocalAppNavController` is provided at the root for deep access when needed.

To add a screen:

1. Add a `@Serializable` route to `AppRoute`.
2. Register `composable<YourRoute> { ... }` in `AppNavGraph`.
3. Navigation changes are logged automatically.

## Memory and performance notes

- Effect streams use a **buffered channel**; collect **once** per screen in `LaunchedEffect` with a stable key (e.g. `viewModel`).
- **Do not** pass `Context` to ViewModels except `@ApplicationContext`.
- Prefer **`collectAsStateWithLifecycle`** for state exposed as `StateFlow`.
- Heavy work runs in `viewModelScope` / `launchIO` on `BaseViewModel` with centralized error logging.

## Base activity

`BaseActivity` is optional scaffolding for non-Compose or hybrid flows. The main entry uses `ComponentActivity` + Compose only; lifecycle logging is applied directly on `CalStuffMainActivity` to avoid duplicate observers.
