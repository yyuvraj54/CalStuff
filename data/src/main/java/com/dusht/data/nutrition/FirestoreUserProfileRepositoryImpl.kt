package com.dusht.data.nutrition

import com.dusht.data.nutrition.dto.UserProfileDto
import com.dusht.data.nutrition.dto.toDto
import com.dusht.shared.model.UserProfile
import com.dusht.shared.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : UserProfileRepository {

    override fun currentUserId(): String? = auth.currentUser?.uid

    private fun userDoc(uid: String) =
        firestore.collection("users").document(uid)

    override fun observeProfile(): Flow<UserProfile?> {
        val uid = currentUserId() ?: return flowOf(null)
        return callbackFlow {
            val listener = userDoc(uid).addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObject(UserProfileDto::class.java)?.toDomain())
            }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        val uid = currentUserId()
            ?: return Result.failure(IllegalStateException("User not authenticated"))
        return runCatching {
            userDoc(uid).set(profile.toDto().copy(uid = uid)).await()
        }
    }

    override suspend fun getProfile(): UserProfile? {
        val uid = currentUserId() ?: return null
        return runCatching {
            userDoc(uid).get().await().toObject(UserProfileDto::class.java)?.toDomain()
        }.getOrNull()
    }
}
