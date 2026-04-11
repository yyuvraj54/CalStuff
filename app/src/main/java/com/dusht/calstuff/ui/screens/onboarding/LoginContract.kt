package com.dusht.calstuff.ui.screens.onboarding

import android.app.Activity
import androidx.activity.result.ActivityResult
import com.dusht.calstuff.utils.base.ViewEffect
import com.dusht.calstuff.utils.base.ViewEvent
import com.dusht.calstuff.utils.base.ViewState

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val phoneLoginPhase: PhoneLoginPhase = PhoneLoginPhase.PhoneEntry,
    val phoneDigits: String = "",
    val otpDigits: String = "",
    val pendingVerificationId: String? = null,
) : ViewState

enum class PhoneLoginPhase {
    /** Phone number entry + Continue to request SMS. */
    PhoneEntry,
    /** OTP entry after code sent (or staging mock). */
    OtpEntry,
}

sealed interface LoginEvent : ViewEvent {
    data object GoogleSignInClicked : LoginEvent
    data class LegacyActivityResult(val result: ActivityResult) : LoginEvent
    data object ErrorConsumed : LoginEvent

    data class PhoneDigitsChanged(val value: String) : LoginEvent
    data class OtpDigitsChanged(val value: String) : LoginEvent
    data class PhoneContinue(val activity: Activity) : LoginEvent
    data object PhoneVerifyOtp : LoginEvent
}

sealed interface LoginEffect : ViewEffect {
    data object NavigateAfterLogin : LoginEffect
}
