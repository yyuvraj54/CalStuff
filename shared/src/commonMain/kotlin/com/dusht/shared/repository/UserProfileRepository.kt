package com.dusht.shared.repository

import com.dusht.shared.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Contract for persisting and observing user profile data.
 *
 * Implementations:
 *  - Android/Firebase: FirestoreUserProfileRepositoryImpl (data module)
 *  - Future custom backend: swap binding in DataModule — no ViewModel changes needed.
 */
interface UserProfileRepository {

    /** UID of the currently authenticated user, or null if not logged in. */
    fun currentUserId(): String?

    /** Live stream of the current user's profile. Emits null if not yet created. */
    fun observeProfile(): Flow<UserProfile?>

    /** Saves (creates or overwrites) the user's profile document. */
    suspend fun saveProfile(profile: UserProfile): Result<Unit>

    /** One-shot fetch. Returns null if no profile exists yet. */
    suspend fun getProfile(): UserProfile?
}
