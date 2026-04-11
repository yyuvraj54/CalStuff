# CalStuff — cross-platform plan, architecture & networking

This document is the **single reference** for building Android + iOS with shared logic, **debug mocks** vs **production backends**, and **future iOS-only surfaces** (e.g. Apple Watch). It complements [`ARCHITECTURE.md`](../ARCHITECTURE.md) (Android MVI/Hilt today) and [`shared/README.md`](../shared/README.md) (KMP module).

---

## 1. Goals (what “good” looks like)

| Goal | How |
|------|-----|
| **Feature parity** (phone app) | Same **behaviour** and **API contracts** on Android and iOS; **native UI** on each platform. |
| **Scalable shared core** | Business rules, models, and networking **abstractions** live in **`:shared` `commonMain`**. Platform code is thin. |
| **Prod vs debug** | **Production**: real HTTPS API + real persistence. **Debug**: same code paths, **mock HTTP responses** from JSON files (no duplicate ViewModels). |
| **Platform-specific features** | Things only Apple needs (Watch, Live Activities, App Clips) sit behind **interfaces** implemented in **iOS-only** targets; Android does not depend on them. |
| **Documentation** | Architecture decisions live in `docs/`; module READMEs stay short and link here. |

---

## 2. Mental model: three rings

```mermaid
flowchart TB
  subgraph shared["`:shared` — Kotlin Multiplatform"]
    CM["commonMain: models, use cases, repository interfaces, ApiClient abstraction, DTOs"]
    AM["androidMain: actuals — optional"]
    IM["iosMain: actuals — optional"]
  end
  subgraph android[":app + :data — Android"]
    Compose["Compose UI + MVI ViewModels"]
    Hilt["Hilt DI"]
    AFire["Firebase / Play Services"]
  end
  subgraph ios["iOS — Xcode"]
    SwiftUI["SwiftUI + observation"]
    IFire["Firebase iOS SDKs"]
    Watch["watchOS app — later"]
  end
  CM --> Compose
  CM --> SwiftUI
  AM --> AFire
  IM --> IFire
  SwiftUI --> Watch
```

- **Inner ring (`commonMain`)**: pure Kotlin, no Android/iOS SDKs — the **contract** for your product.
- **Middle ring**: Android (`:data`) and iOS (Swift + optional Kotlin `iosMain`) **implement** those contracts (HTTP, DB, auth, secure storage).
- **Outer ring**: **UI** and **platform-exclusive** apps (Watch, widgets) talk only to **shared use cases** or **shared repositories**, not to raw Retrofit/URLSession everywhere.

---

## 3. Module topology (recommended evolution)

| Module / target | Responsibility |
|-----------------|----------------|
| **`:shared`** | **DTOs** (`kotlinx.serialization`), **repository interfaces**, **use cases**, **network abstraction** (`HttpClient` / `ApiService` interfaces), **Result** / error mapping. |
| **`:data` (Android)** | Hilt modules, **Ktor** or OkHttp **engine**, Room/DataStore, Firebase **Android** bindings, `actual` for `expect` when needed. |
| **`:app` (Android)** | Compose, MVI ViewModels (thin: delegate to use cases from `:shared`). |
| **iOS app (Xcode)** | SwiftUI, embed **`CalStuffShared.framework`**, Firebase **iOS**, Keychain/UserDefaults via Swift or `iosMain`. |
| **watchOS (future)** | Separate target; **Watch-specific UI**; calls **same use cases** via a small **watch-shared** layer or iOS app as intermediary (WatchConnectivity) — decide when you add Watch; document in `docs/APPLE_WATCH.md` when created. |

**Rule of thumb:** if both apps need it, it belongs in **`commonMain`** or a **shared interface** with two implementations. If only one platform needs it, keep it out of `commonMain`.

---

## 4. Networking: production vs debug mocks

### 4.1 Principles

1. **One API surface** in `commonMain`: functions like `UserApi.getProfile(): Result<ProfileDto>` or a single **`CalStuffApi`** class used by repositories.
2. **No `if (DEBUG)` inside use cases** scattered everywhere. Prefer **dependency injection** of either:
   - a **real** `HttpClientEngine` / transport, or  
   - a **mock** transport that returns bytes from JSON files.
3. **DTOs and paths** are shared; only the **transport** changes between prod and debug.

### 4.2 Production (real backend + DB)

- **HTTPS** to your API base URL (per environment: staging / prod via build config or `expect` config provider).
- **Authentication**: attach tokens in one **interceptor** / Ktor plugin (implemented per platform if needed, or in `commonMain` if pure Kotlin).
- **Persistence**: server is source of truth; **local DB** (SQLDelight in KMP or platform DB) caches for offline — plan **repository** methods in `commonMain`, **actual** storage in `androidMain` / `iosMain` or Swift.

### 4.3 Debug: JSON mocks “as if” from backend

**Recommended pattern for KMP:**

1. Put JSON files under a predictable tree, e.g.  
   `shared/src/commonMain/mocks/`  
   (or `androidApp/src/debug/assets/mock-api/` + **mirror** in iOS bundle for Swift-only phase — **prefer common resources** once KMP resources are wired).

2. Define in **`commonMain`**:

   ```text
   interface MockAssetLoader {
       suspend fun load(relativePath: String): String  // JSON body
   }
   ```

3. Provide **`expect`/`actual`** (or inject platform implementations):

   - **Android `actual`**: read from `assets` or `resources`.
   - **iOS `actual`**: read from **Bundle** resource.

4. **Ktor `MockEngine`** (or a small fake implementation of your `ApiClient`) in **debug** builds:

   - Map `GET /v1/user` → `mocks/user_me.json`
   - Map `POST /v1/login` → `mocks/login_success.json`

5. **Gradle**: use **product flavors** or **build types** + `buildKonfig` / `expect fun isMockNetworkEnabled(): Boolean` so **release** never ships mock loaders.

**Benefits:** Android Studio and Xcode both exercise **the same parsing and use-case code** as production; only the **wire** differs.

### 4.4 What not to do long-term

- Duplicating mock logic in Swift and Kotlin for the same endpoint — **converge on KMP** for API + parsing.
- Hardcoding huge JSON strings in source files — use **files** and version them in git.

---

## 5. Feature parity vs platform-only features

| Area | Android + iOS (shared behaviour) | iOS-only (example) |
|------|-----------------------------------|---------------------|
| Login, session, main tabs | Same flows & rules | — |
| API client, DTOs | `commonMain` | — |
| Push | FCM vs APNs — **different impl**, same **repository interface** | — |
| Watch | — | Companion app; sync via watch connectivity or shared KMP use cases |

Add a **`PlatformCapabilities`** interface in `commonMain` if you need “isWatchCompanionAvailable” etc., with **no-op** on Android.

---

## 6. Phased roadmap (practical order)

| Phase | Focus | Outcome |
|-------|--------|---------|
| **A — Stabilize** | Keep MVI on Android; SwiftUI parity for login/home; embed **`CalStuffShared`** | One framework, thin duplication |
| **B — Shared API layer** | `kotlinx.serialization` DTOs + Ktor client in **`commonMain`**; Android `:data` supplies engine | Single contract for JSON |
| **C — Mock vs prod** | `MockAssetLoader` + `MockEngine` for debug; prod URL from config | Debug without backend |
| **D — Repositories in KMP** | Move session + main repositories to **`commonMain`**; Android/iOS **actual** for storage | Less duplicated logic |
| **E — iOS-specific** | Watch / widgets; **new targets**, same use cases | Scalable without polluting Android |

---

## 7. If you only know Android today: iOS learning map

1. **Swift** basics (optionals, structs vs classes, protocols) — Apple’s “Swift Tour”.
2. **SwiftUI** lifecycle (`@State`, `@Observable` / `ObservableObject`, navigation).
3. **Xcode**: schemes, signing, Simulator, breakpoints.
4. **Human Interface Guidelines** skimming — spacing and navigation differ from Material.
5. **Firebase iOS** docs for parity with your Android auth.

Your **architecture** stays familiar: **MVI-like** state in a ViewModel-equivalent (`ObservableObject`), **one-way data flow**, **repositories** — same ideas, different syntax.

---

## 8. Documentation index (keep this updated)

| Document | Purpose |
|----------|---------|
| [`ARCHITECTURE.md`](../ARCHITECTURE.md) | Android modules, MVI, Hilt, navigation, logging |
| [`shared/README.md`](../shared/README.md) | KMP `shared` module, Gradle tasks for iOS framework |
| **`docs/CROSS_PLATFORM_ARCHITECTURE.md`** (this file) | Cross-platform strategy, networking, mocks, roadmap |
| [`ios/README.md`](../ios/README.md) | SwiftUI login drop-in, Xcode steps |

When you add Watch or a mock server convention, add **`docs/APPLE_WATCH.md`** or **`docs/MOCK_API_CONVENTIONS.md`** and link them here.

---

## 9. Summary

- **Scale** by growing **`commonMain`** (contracts + use cases + API + DTOs), not by copying business logic into Swift.
- **Prod vs debug** differ by **which network implementation is injected**, not by forking every screen.
- **iOS-only features** stay in **iOS targets** and depend on **shared** behaviour through clear interfaces.
- **Document** decisions in `docs/` so future you (and collaborators) know why the split exists.

This is the architecture that supports **real backend + DB in prod** and **JSON-driven mocks in debug** without painting yourself into a corner.
