package com.dusht.calstuff.vm

import androidx.lifecycle.ViewModel
import com.dusht.core.logging.AppLogger
import com.dusht.shared.session.DisplayNameStore
import com.dusht.shared.repository.UserProfileRepository
import com.dusht.shared.session.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val displayNameStore: DisplayNameStore,
) : ViewModel() {

    fun isLoggedIn(): Boolean = userSessionRepository.isLoggedIn()

    fun hasCompletedOnboarding(): Boolean = userSessionRepository.hasCompletedOnboarding()

    fun completeOnboarding() {
        AppLogger.app(message = "completeOnboarding — prefs onboarding_completed=true")
        userSessionRepository.setOnboardingCompleted(true)
    }

    fun logout() {
        AppLogger.app(message = "logout — clearing session + display name cache")
        userSessionRepository.setLoggedIn(false)
        userSessionRepository.setOnboardingCompleted(false)
        displayNameStore.clear()
    }

    /** If the user reinstalled the app, local onboarding flag may be false while Firestore has a full profile. */
    suspend fun syncOnboardingFromFirestoreIfLoggedIn() {
        if (!userSessionRepository.isLoggedIn()) return
        val profile = userProfileRepository.getProfile()
        if (profile != null && profile.name.isNotBlank()) {
            displayNameStore.set(profile.name.trim())
            userSessionRepository.setOnboardingCompleted(true)
        }
    }
}