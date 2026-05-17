package com.dusht.data.session

import com.dusht.shared.session.DisplayNameStore
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayNameStoreImpl @Inject constructor(
    @ApplicationContext context: Context,
) : DisplayNameStore {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun get(): String? =
        prefs.getString(KEY_DISPLAY_NAME, null)?.takeIf { it.isNotBlank() }

    override fun set(name: String) {
        prefs.edit().putString(KEY_DISPLAY_NAME, name.trim()).apply()
    }

    override fun clear() {
        prefs.edit().remove(KEY_DISPLAY_NAME).apply()
    }

    private companion object {
        const val PREFS_NAME = "calstuff_encrypted_user_prefs"
        const val KEY_DISPLAY_NAME = "display_name"
    }
}
