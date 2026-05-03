package com.dusht.shared.profile

/**
 * Server-driven check: whether the user still needs profile onboarding.
 * Android: mock in `:data`. iOS can mirror with Swift or shared KMP later.
 */
data class ProfileCompleteness(val isProfileComplete: Boolean)

interface ProfileGateRepository {
    suspend fun fetchProfileCompleteness(): ProfileCompleteness
}
