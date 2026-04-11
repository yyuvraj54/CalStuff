package com.dusht.calstuff.auth

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException

/**
 * Maps Firebase exceptions to short, actionable copy for toasts.
 * [ERROR_OPERATION_NOT_ALLOWED] usually means Phone provider is off in the Firebase Console.
 */
fun Throwable.toAuthUserMessage(): String {
    return when (this) {
        is FirebaseAuthException -> when (errorCode) {
            "ERROR_OPERATION_NOT_ALLOWED" ->
                "Phone sign-in is turned off. In Firebase Console: Authentication → Sign-in method → enable Phone."
            "ERROR_INVALID_PHONE_NUMBER" ->
                "Check the number format (e.g. 10 digits for US, or include country code)."
            "ERROR_TOO_MANY_REQUESTS" ->
                "Too many attempts. Wait a bit and try again."
            "ERROR_SESSION_EXPIRED", "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                localizedMessage ?: message ?: "Sign-in error ($errorCode)"
            else -> localizedMessage ?: message ?: "Couldn’t verify phone ($errorCode)"
        }
        is FirebaseException -> localizedMessage ?: message ?: "Network or Firebase error"
        else -> message ?: "Something went wrong"
    }
}
