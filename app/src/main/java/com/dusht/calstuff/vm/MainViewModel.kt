package com.dusht.calstuff.vm

import androidx.lifecycle.ViewModel
import com.dusht.shared.session.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    fun isLoggedIn(): Boolean = userSessionRepository.isLoggedIn()

    fun hasCompletedOnboarding(): Boolean = userSessionRepository.hasCompletedOnboarding()

    fun completeOnboarding() {
        userSessionRepository.setOnboardingCompleted(true)
    }

    fun logout() {
        userSessionRepository.setLoggedIn(false)
    }
}