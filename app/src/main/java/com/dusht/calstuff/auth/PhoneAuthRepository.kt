package com.dusht.calstuff.auth

import android.app.Activity
import com.dusht.calstuff.BuildConfig
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Firebase phone auth with a staging-only shortcut for QA
 * ([PhoneAuthRepository.Companion.STAGING_TEST_PHONE_DIGITS] + [STAGING_TEST_OTP]).
 * Production uses real SMS / instant verification.
 */
@Singleton
class PhoneAuthRepository @Inject constructor() {

    private val auth get() = Firebase.auth

    suspend fun requestSmsCode(activity: Activity, phoneDigits: String): Result<PhoneVerificationStart> {
        val digits = phoneDigits.filter { it.isDigit() }
        if (digits.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a phone number"))
        }

        if (BuildConfig.IS_STAGING && digits == STAGING_TEST_PHONE_DIGITS) {
            return Result.success(PhoneVerificationStart.SmsCodeRequired(STAGING_MOCK_VERIFICATION_ID))
        }

        val e164 = digitsToE164(digits)
        return suspendCancellableCoroutine { cont ->
            val resumed = AtomicBoolean(false)
            fun resumeOnce(value: Result<PhoneVerificationStart>) {
                if (resumed.compareAndSet(false, true)) {
                    cont.resume(value)
                }
            }

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            resumeOnce(
                                if (task.isSuccessful) {
                                    Result.success(PhoneVerificationStart.SignedInByFirebase)
                                } else {
                                    Result.failure(task.exception ?: Exception("Phone sign-in failed"))
                                }
                            )
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    resumeOnce(Result.failure(e))
                }

                override fun onCodeSent(
                    verificationId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    resumeOnce(Result.success(PhoneVerificationStart.SmsCodeRequired(verificationId)))
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(e164)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    suspend fun submitSmsCode(verificationId: String, smsCode: String): Result<Unit> {
        val code = smsCode.trim()
        if (BuildConfig.IS_STAGING && verificationId == STAGING_MOCK_VERIFICATION_ID) {
            return if (code == STAGING_TEST_OTP) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Invalid code for staging test account"))
            }
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return runCatching {
            auth.signInWithCredential(credential).await()
            Unit
        }
    }

    companion object {
        internal const val STAGING_MOCK_VERIFICATION_ID = "staging_mock_vid"

        /** Staging flavor only — matches QA test login (11 digits). */
        const val STAGING_TEST_PHONE_DIGITS = "12345678910"
        const val STAGING_TEST_OTP = "999999"

        internal fun digitsToE164(digits: String): String {
            val d = digits.filter { it.isDigit() }
            return when {
                d.length == 11 && d.startsWith("1") -> "+$d"
                d.length == 10 -> "+1$d"
                else -> "+$d"
            }
        }
    }
}

sealed class PhoneVerificationStart {
    data class SmsCodeRequired(val verificationId: String) : PhoneVerificationStart()
    /** Instant verification completed; Firebase user is already signed in. */
    data object SignedInByFirebase : PhoneVerificationStart()
}
