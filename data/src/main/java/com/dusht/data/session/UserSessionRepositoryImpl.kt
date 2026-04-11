package com.dusht.data.session

import android.content.Context
import android.content.SharedPreferences
import com.dusht.shared.session.UserSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : UserSessionRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isLoggedIn(): Boolean =
        prefs.getBoolean(KEY_LOGGED_IN, false)

    override fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()
    }

    override fun hasCompletedOnboarding(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING, false)

    override fun setOnboardingCompleted(value: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING, value).apply()
    }

    /** Matches legacy [com.dusht.calstuff] SharedPreferences keys for upgrades. */
    private companion object {
        const val PREFS_NAME = "app_prefs"
        const val KEY_LOGGED_IN = "is_logged_in"
        const val KEY_ONBOARDING = "onboarding_completed"
    }
}
