package com.dusht.calstuff.login

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.dusht.calstuff.R
import com.dusht.core.logging.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object GoogleSignInUtils {

    fun doGoogleSignIn(
        context: Context,
        scope: CoroutineScope,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val appContext = context.applicationContext
        AppLogger.app(
            message = "Google sign-in started",
            extras = mapOf(
                "sdkInt" to Build.VERSION.SDK_INT,
                "useCredentialManager" to (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            signInWithCredentialManager(appContext, scope, launcher, onSuccess, onError)
        } else {
            signInWithGoogleClient(appContext, launcher)
        }
    }

    /**
     * Call from [ActivityResultContracts.StartActivityForResult] callback for legacy Google Sign-In (API &lt; 34 path).
     */
    suspend fun completeLegacySignInFromIntent(
        context: Context,
        data: Intent?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (data == null) {
                return@withContext Result.failure(IllegalStateException("Sign-in cancelled or no data"))
            }
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
                ?: return@withContext Result.failure(IllegalStateException("Missing Google ID token"))
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            Firebase.auth.signInWithCredential(credential).await()
            AppLogger.app(message = "Legacy Google sign-in completed", extras = mapOf("uid" to Firebase.auth.currentUser?.uid))
            Result.success(Unit)
        } catch (e: ApiException) {
            AppLogger.app(message = "Legacy Google sign-in failed", throwable = e, extras = mapOf("code" to e.statusCode))
            Result.failure(e)
        } catch (e: Exception) {
            AppLogger.app(message = "Legacy Google sign-in failed", throwable = e)
            Result.failure(e)
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun signInWithCredentialManager(
        context: Context,
        scope: CoroutineScope,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                when (val cred = result.credential) {
                    is CustomCredential -> {
                        if (cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
                            val googleTokenId = googleTokenCredential.idToken
                            val authCredential = GoogleAuthProvider.getCredential(googleTokenId, null)

                            val user = Firebase.auth.signInWithCredential(authCredential).await().user

                            if (user != null && !user.isAnonymous) {
                                AppLogger.app(
                                    message = "Credential Manager sign-in success",
                                    extras = mapOf("uid" to user.uid)
                                )
                                onSuccess()
                            } else {
                                onError("Anonymous or null user")
                            }
                        } else {
                            onError("Unexpected credential type: ${cred.type}")
                        }
                    }
                    else -> onError("Unsupported credential: ${cred::class.simpleName}")
                }
            } catch (e: NoCredentialException) {
                AppLogger.app(message = "No credential; launching account add", extras = mapOf("error" to e.message))
                launcher?.launch(getAddAccountIntent())
            } catch (e: GetCredentialException) {
                AppLogger.app(message = "GetCredentialException", throwable = e)
                onError(e.message ?: "Credential error")
            }
        }
    }

    private fun getAddAccountIntent(): Intent {
        return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
        }
    }

    private fun signInWithGoogleClient(
        context: Context,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?
    ) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
        AppLogger.app(message = "Launching legacy Google Sign-In intent")
        launcher?.launch(googleSignInClient.signInIntent)
    }
}
