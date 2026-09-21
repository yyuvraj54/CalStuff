package com.dusht.shared.session

import platform.Foundation.NSUserDefaults

/**
 * iOS actual — mirrors the Android `:data` implementation's semantics (two independent
 * booleans backed by simple prefs) using `NSUserDefaults` instead of `SharedPreferences`.
 * Constructible directly from Swift: `UserSessionRepositoryImpl()`.
 */
class UserSessionRepositoryImpl : UserSessionRepository {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun isLoggedIn(): Boolean = defaults.boolForKey(KEY_LOGGED_IN)

    override fun setLoggedIn(value: Boolean) {
        defaults.setBool(value, KEY_LOGGED_IN)
    }

    override fun hasCompletedOnboarding(): Boolean = defaults.boolForKey(KEY_ONBOARDING)

    override fun setOnboardingCompleted(value: Boolean) {
        defaults.setBool(value, KEY_ONBOARDING)
    }

    private companion object {
        const val KEY_LOGGED_IN = "is_logged_in"
        const val KEY_ONBOARDING = "onboarding_completed"
    }
}
