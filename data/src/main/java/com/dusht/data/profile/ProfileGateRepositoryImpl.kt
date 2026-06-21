package com.dusht.data.profile

import com.dusht.core.logging.AppLogger
import com.dusht.shared.profile.ProfileCompleteness
import com.dusht.shared.profile.ProfileGateRepository
import com.dusht.shared.repository.UserProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileGateRepositoryImpl @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) : ProfileGateRepository {

    override suspend fun fetchProfileCompleteness(): ProfileCompleteness {
        val profile = userProfileRepository.getProfile()
        val complete = profile != null && profile.name.isNotBlank()
        AppLogger.firebase(
            message = "fetchProfileCompleteness",
            extras = mapOf("isProfileComplete" to complete),
        )
        return ProfileCompleteness(isProfileComplete = complete)
    }
}
