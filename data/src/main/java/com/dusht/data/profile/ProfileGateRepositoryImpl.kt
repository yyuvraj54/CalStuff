package com.dusht.data.profile

import com.dusht.shared.profile.ProfileCompleteness
import com.dusht.shared.profile.ProfileGateRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock gate: returns incomplete so profile onboarding runs. Flip [mockProfileComplete] to test skip.
 */
@Singleton
class ProfileGateRepositoryImpl @Inject constructor() : ProfileGateRepository {

    var mockProfileComplete: Boolean = false

    override suspend fun fetchProfileCompleteness(): ProfileCompleteness {
        delay(400)
        return ProfileCompleteness(isProfileComplete = mockProfileComplete)
    }
}
