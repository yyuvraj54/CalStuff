package com.dusht.data.session

import android.content.Context
import android.content.SharedPreferences
import com.dusht.shared.session.ThemeMode
import com.dusht.shared.session.ThemePreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : ThemePreferenceRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readThemeMode())
    override val themeMode: StateFlow<ThemeMode> = _themeMode

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun readThemeMode(): ThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
    }

    /** Matches [com.dusht.data.session.UserSessionRepositoryImpl]'s prefs file. */
    private companion object {
        const val PREFS_NAME = "app_prefs"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
