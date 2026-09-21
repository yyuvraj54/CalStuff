package com.dusht.shared.profile

import com.dusht.shared.model.UserProfile

/**
 * Server-driven check: whether the user still needs profile onboarding.
 * Android: mock in `:data`. iOS mirrors with Swift, both call [isProfileComplete].
 */
data class ProfileCompleteness(val isProfileComplete: Boolean)

interface ProfileGateRepository {
    suspend fun fetchProfileCompleteness(): ProfileCompleteness
}

/** A profile is "complete enough" to skip onboarding once it exists and has a name. */
fun isProfileComplete(profile: UserProfile?): Boolean =
    profile != null && profile.name.isNotBlank()
