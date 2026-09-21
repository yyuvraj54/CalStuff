package com.dusht.shared.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSUserDefaults

/**
 * iOS actual — mirrors the Android `:data` implementation (in-memory `StateFlow` seeded once
 * from prefs, kept in sync on every write) using `NSUserDefaults` instead of `SharedPreferences`.
 * `MutableStateFlow` isn't constructible from Swift (kotlinx-coroutines-core isn't `export()`ed
 * into the framework), so this — unlike [UserSessionRepositoryImpl] — could only live here, not
 * in Swift. Constructible directly from Swift: `ThemePreferenceRepositoryImpl()`.
 */
class ThemePreferenceRepositoryImpl : ThemePreferenceRepository {

    private val defaults = NSUserDefaults.standardUserDefaults
    private val _themeMode = MutableStateFlow(readThemeMode())
    override val themeMode: StateFlow<ThemeMode> = _themeMode

    override fun setThemeMode(mode: ThemeMode) {
        defaults.setObject(mode.name, KEY_THEME_MODE)
        _themeMode.value = mode
    }

    private fun readThemeMode(): ThemeMode {
        val stored = defaults.stringForKey(KEY_THEME_MODE) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
