package com.dusht.shared.session

import kotlinx.coroutines.flow.StateFlow

/**
 * User's persisted display-theme preference (Light/Dark/System), shared between Android
 * (Compose) and iOS (SwiftUI). Exposed as a [StateFlow] — unlike the plain sync get/set on
 * [UserSessionRepository] — because a theme change must recompose the whole UI live, not
 * just be read once at startup.
 *
 * Android: implemented in `:data`. iOS: provide an implementation in Swift or Kotlin `iosMain`.
 */
interface ThemePreferenceRepository {
    val themeMode: StateFlow<ThemeMode>
    fun setThemeMode(mode: ThemeMode)
}
