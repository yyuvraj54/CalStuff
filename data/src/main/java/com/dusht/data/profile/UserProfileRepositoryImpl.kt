package com.dusht.data.profile

import com.dusht.core.logging.AppLogger
import com.dusht.shared.session.DisplayNameStore
import com.dusht.shared.profile.UserProfile
import com.dusht.shared.profile.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val displayNameStore: DisplayNameStore,
) : UserProfileRepository {

    override suspend fun loadProfile(): UserProfile? {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            AppLogger.firebase(message = "loadProfile skipped — no FirebaseAuth user")
            return null
        }
        AppLogger.firebase(message = "loadProfile start", extras = mapOf("uid" to uid))
        val doc = fetchUserDocument(uid) ?: run {
            AppLogger.firebase(message = "loadProfile — no document (offline / empty cache)")
            return null
        }
        if (!doc.exists()) {
            AppLogger.firebase(message = "loadProfile — document missing", extras = mapOf("uid" to uid))
            return null
        }
        val profile = UserProfile(
            name = doc.getString(F_NAME).orEmpty(),
            age = doc.intField(F_AGE),
            gender = doc.getString(F_GENDER),
            heightCm = doc.intField(F_HEIGHT_CM),
            weightKg = doc.intField(F_WEIGHT_KG),
            activity = doc.getString(F_ACTIVITY),
        )
        val name = profile.name.trim()
        if (name.isNotEmpty()) displayNameStore.set(name)
        AppLogger.firebase(message = "loadProfile success", extras = mapOf("uid" to uid, "name" to name))
        return profile
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        AppLogger.firebase(
            message = "saveProfile start",
            extras = mapOf("uid" to uid, "isComplete" to profile.isComplete()),
        )
        require(profile.isComplete()) { "Cannot save incomplete profile" }
        val data = mapOf(
            F_NAME to profile.name.trim(),
            F_AGE to profile.age!!,
            F_GENDER to profile.gender!!,
            F_HEIGHT_CM to profile.heightCm!!,
            F_WEIGHT_KG to profile.weightKg!!,
            F_ACTIVITY to profile.activity!!,
        )
        firestore.collection(USERS).document(uid).set(data, SetOptions.merge()).await()
        displayNameStore.set(profile.name.trim())
        AppLogger.firebase(message = "saveProfile success", extras = mapOf("uid" to uid))
    }.onFailure { e ->
        AppLogger.firebase(
            message = "saveProfile failed",
            extras = mapOf("error" to (e.message ?: e.javaClass.simpleName)),
            throwable = e,
        )
    }

    /**
     * Default fetch throws when offline with no local cache; fall back to [Source.CACHE] if available.
     */
    private suspend fun fetchUserDocument(uid: String): DocumentSnapshot? {
        val ref = firestore.collection(USERS).document(uid)
        runCatching { ref.get().await() }.getOrNull()?.let { return it }
        AppLogger.firebase(message = "fetchUserDocument — default get failed, trying Source.CACHE")
        return runCatching { ref.get(Source.CACHE).await() }.getOrNull()
    }

    private fun DocumentSnapshot.intField(field: String): Int? =
        (get(field) as? Number)?.toInt()

    private companion object {
        const val USERS = "users"
        const val F_NAME = "name"
        const val F_AGE = "age"
        const val F_GENDER = "gender"
        const val F_HEIGHT_CM = "heightCm"
        const val F_WEIGHT_KG = "weightKg"
        const val F_ACTIVITY = "activity"
    }
}
