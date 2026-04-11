package com.dusht.calstuff.vm

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import com.dusht.calstuff.auth.PhoneAuthRepository
import com.dusht.calstuff.auth.PhoneVerificationStart
import com.dusht.calstuff.auth.toAuthUserMessage
import com.dusht.calstuff.login.GoogleSignInUtils
import com.dusht.calstuff.ui.screens.onboarding.LoginEffect
import com.dusht.calstuff.ui.screens.onboarding.LoginEvent
import com.dusht.calstuff.ui.screens.onboarding.LoginUiState
import com.dusht.calstuff.ui.screens.onboarding.PhoneLoginPhase
import com.dusht.calstuff.utils.base.BaseViewModel
import com.dusht.core.logging.AppLogger
import com.dusht.shared.session.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userSessionRepository: UserSessionRepository,
    private val phoneAuthRepository: PhoneAuthRepository,
) : BaseViewModel<LoginUiState, LoginEvent, LoginEffect>(LoginUiState()) {

    override fun handleEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.GoogleSignInClicked -> {
                AppLogger.app(message = "LoginEvent.GoogleSignInClicked")
                setState { copy(errorMessage = null) }
            }
            is LoginEvent.LegacyActivityResult -> handleLegacyResult(event.result)
            LoginEvent.ErrorConsumed -> setState { copy(errorMessage = null) }

            is LoginEvent.PhoneDigitsChanged -> {
                val digits = event.value.filter { it.isDigit() }.take(PHONE_MAX_DIGITS)
                val previous = currentState.phoneDigits.filter { it.isDigit() }
                val wasOtp = currentState.phoneLoginPhase == PhoneLoginPhase.OtpEntry
                val phoneChangedWhileOtp = wasOtp && digits != previous
                setState {
                    copy(
                        phoneDigits = digits,
                        phoneLoginPhase = when {
                            digits.isEmpty() -> PhoneLoginPhase.PhoneEntry
                            phoneChangedWhileOtp -> PhoneLoginPhase.PhoneEntry
                            else -> phoneLoginPhase
                        },
                        otpDigits = if (digits.isEmpty() || phoneChangedWhileOtp) "" else otpDigits,
                        pendingVerificationId = if (digits.isEmpty() || phoneChangedWhileOtp) {
                            null
                        } else {
                            pendingVerificationId
                        }
                    )
                }
            }
            is LoginEvent.OtpDigitsChanged -> {
                val digits = event.value.filter { it.isDigit() }.take(OTP_LENGTH)
                setState { copy(otpDigits = digits) }
            }
            is LoginEvent.PhoneContinue -> sendPhoneCode(event.activity)
            LoginEvent.PhoneVerifyOtp -> verifyOtp()
        }
    }

    fun onGoogleSignInSuccess() {
        viewModelScope.launch {
            userSessionRepository.setLoggedIn(true)
            AppLogger.app(message = "Session persisted: logged in")
            sendEffect(LoginEffect.NavigateAfterLogin)
        }
    }

    private fun sendPhoneCode(activity: Activity) {
        val digits = currentState.phoneDigits.filter { it.isDigit() }
        if (digits.length < MIN_PHONE_DIGITS) {
            setState { copy(errorMessage = "Enter a valid phone number") }
            return
        }
        launchIO {
            setState { copy(isLoading = true, errorMessage = null) }
            val result = phoneAuthRepository.requestSmsCode(activity, currentState.phoneDigits)
            result.fold(
                onSuccess = { start ->
                    when (start) {
                        is PhoneVerificationStart.SmsCodeRequired -> {
                            AppLogger.app(
                                message = "phone_code_sent",
                                extras = mapOf("stagingMock" to (start.verificationId == PhoneAuthRepository.STAGING_MOCK_VERIFICATION_ID))
                            )
                            setState {
                                copy(
                                    pendingVerificationId = start.verificationId,
                                    phoneLoginPhase = PhoneLoginPhase.OtpEntry,
                                    otpDigits = ""
                                )
                            }
                        }
                        PhoneVerificationStart.SignedInByFirebase -> {
                            AppLogger.app(message = "phone_instant_verification")
                            finishPhoneLogin()
                        }
                    }
                },
                onFailure = { e ->
                    setState { copy(errorMessage = e.toAuthUserMessage()) }
                }
            )
            setState { copy(isLoading = false) }
        }
    }

    private fun verifyOtp() {
        val vid = currentState.pendingVerificationId
        val code = currentState.otpDigits
        if (vid.isNullOrBlank() || code.length < OTP_LENGTH) {
            setState { copy(errorMessage = "Enter the 6-digit code") }
            return
        }
        launchIO {
            setState { copy(isLoading = true, errorMessage = null) }
            val result = phoneAuthRepository.submitSmsCode(vid, code)
            result.fold(
                onSuccess = {
                    AppLogger.app(message = "phone_otp_verified")
                    finishPhoneLogin()
                },
                onFailure = { e ->
                    setState { copy(errorMessage = e.toAuthUserMessage()) }
                }
            )
            setState { copy(isLoading = false) }
        }
    }

    private fun finishPhoneLogin() {
        userSessionRepository.setLoggedIn(true)
        AppLogger.app(message = "Session persisted: phone login")
        sendEffect(LoginEffect.NavigateAfterLogin)
    }

    private fun handleLegacyResult(result: ActivityResult) {
        launchIO {
            setState { copy(isLoading = true, errorMessage = null) }
            val outcome = GoogleSignInUtils.completeLegacySignInFromIntent(appContext, result.data)
            outcome.fold(
                onSuccess = {
                    userSessionRepository.setLoggedIn(true)
                    AppLogger.app(message = "Legacy sign-in persisted")
                    sendEffect(LoginEffect.NavigateAfterLogin)
                },
                onFailure = { e ->
                    setState { copy(errorMessage = e.message ?: "Sign-in failed") }
                }
            )
            setState { copy(isLoading = false) }
        }
    }

    override fun handleError(exception: Exception) {
        super.handleError(exception)
        setState { copy(isLoading = false, errorMessage = exception.message) }
    }

    private companion object {
        const val PHONE_MAX_DIGITS = 11
        const val MIN_PHONE_DIGITS = 10
        const val OTP_LENGTH = 6
    }
}
