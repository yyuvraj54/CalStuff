package com.dusht.shared.profile

interface UserProfileRepository {
    suspend fun loadProfile(): UserProfile?
    suspend fun saveProfile(profile: UserProfile): Result<Unit>
}
