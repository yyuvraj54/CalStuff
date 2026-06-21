package com.dusht.data.nutrition

import com.dusht.core.logging.AppLogger
import com.dusht.data.local.dao.UserProfileDao
import com.dusht.data.local.entity.toEntity
import com.dusht.data.nutrition.dto.UserProfileDto
import com.dusht.data.nutrition.dto.toDto
import com.dusht.shared.model.UserProfile
import com.dusht.shared.repository.UserProfileRepository
import com.dusht.shared.session.DisplayNameStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val displayNameStore: DisplayNameStore,
) : UserProfileRepository {

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun currentUserId(): String? = auth.currentUser?.uid

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)

    override fun observeProfile(): Flow<UserProfile?> {
        val uid = currentUserId() ?: return flowOf(null)
        // Seed Room from Firestore on first observe; Room drives the UI thereafter.
        syncScope.launch { syncProfileFromFirestore(uid) }
        return userProfileDao.observeProfile(uid).map { it?.toDomain() }
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        val uid = currentUserId()
        if (uid == null) {
            AppLogger.firebase(message = "saveProfile skipped — no FirebaseAuth user")
            return Result.failure(IllegalStateException("Not authenticated"))
        }
        AppLogger.firebase(message = "saveProfile start", extras = mapOf("uid" to uid, "name" to profile.name))
        userProfileDao.upsertProfile(profile.toEntity())
        return runCatching {
            userDoc(uid).set(profile.toDto().copy(uid = uid)).await()
            userProfileDao.upsertProfile(profile.toEntity(syncedAt = System.currentTimeMillis()))
            cacheDisplayName(profile.name)
            AppLogger.firebase(message = "saveProfile success", extras = mapOf("uid" to uid))
        }.onFailure { e ->
            cacheDisplayName(profile.name)
            AppLogger.firebase(
                message = "saveProfile failed — name cached locally",
                extras = mapOf("error" to (e.message ?: e.javaClass.simpleName)),
                throwable = e,
            )
        }
    }

    override suspend fun getProfile(): UserProfile? {
        val uid = currentUserId() ?: run {
            AppLogger.firebase(message = "getProfile skipped — no FirebaseAuth user")
            return null
        }
        AppLogger.firebase(message = "getProfile start", extras = mapOf("uid" to uid))
        val local = userProfileDao.getProfile(uid)
        if (local != null) {
            val domain = local.toDomain()
            cacheDisplayName(domain.name)
            AppLogger.firebase(message = "getProfile from Room", extras = mapOf("name" to domain.name))
            return domain
        }
        return runCatching {
            val remote = userDoc(uid).get().await().toObject(UserProfileDto::class.java)
                ?: return null
            val domain = remote.toDomain()
            userProfileDao.upsertProfile(domain.toEntity(syncedAt = System.currentTimeMillis()))
            cacheDisplayName(domain.name)
            AppLogger.firebase(message = "getProfile from Firestore", extras = mapOf("name" to domain.name))
            domain
        }.getOrNull().also { profile ->
            if (profile == null) {
                AppLogger.firebase(message = "getProfile — no profile found")
            }
        }
    }

    private fun cacheDisplayName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) displayNameStore.set(trimmed)
    }

    private suspend fun syncProfileFromFirestore(uid: String) {
        // Skip if already cached locally.
        if (userProfileDao.getProfile(uid) != null) return
        runCatching {
            val remote = userDoc(uid).get().await().toObject(UserProfileDto::class.java) ?: return
            userProfileDao.upsertProfile(remote.toDomain().toEntity(syncedAt = System.currentTimeMillis()))
        }
    }
}
