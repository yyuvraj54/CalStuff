package com.dusht.shared.session

/**
 * Session flags and auth state shared between Android (Compose) and iOS (Swift).
 * Android: implemented in `:data`. iOS: provide an implementation in Swift or Kotlin `iosMain`.
 */
interface UserSessionRepository {
    fun isLoggedIn(): Boolean
    fun setLoggedIn(value: Boolean)

    fun hasCompletedOnboarding(): Boolean
    fun setOnboardingCompleted(value: Boolean)
}
