package com.dusht.calstuff.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dusht.core.logging.AppLogger
import com.dusht.shared.profile.UserProfile
import com.dusht.shared.profile.UserProfileRepository
import com.dusht.shared.session.DisplayNameStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileGender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_SAY,
}

enum class ProfileActivityLevel {
    SEDENTARY,
    LIGHT,
    MODERATE,
    ACTIVE,
    VERY_ACTIVE,
}

data class ProfileOnboardingUiState(
    val stepIndex: Int = 0,
    val name: String = "",
    val ageText: String = "",
    val gender: ProfileGender? = null,
    val heightCmText: String = "",
    val weightKgText: String = "",
    val activity: ProfileActivityLevel? = null,
)

sealed interface ProfileOnboardingContinueResult {
    data object Advanced : ProfileOnboardingContinueResult
    data object Finished : ProfileOnboardingContinueResult
    data object Invalid : ProfileOnboardingContinueResult
}

@HiltViewModel
class ProfileOnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val displayNameStore: DisplayNameStore,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileOnboardingUiState())
    val state: StateFlow<ProfileOnboardingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = userProfileRepository.loadProfile() ?: return@launch
            _state.update { it.withLoadedProfile(saved) }
        }
    }

    fun updateName(value: String) {
        _state.update { it.copy(name = value) }
    }

    fun updateAge(value: String) {
        if (value.length <= 3 && value.all { it.isDigit() }) {
            _state.update { it.copy(ageText = value) }
        }
    }

    fun selectGender(value: ProfileGender) {
        _state.update { it.copy(gender = value) }
    }

    fun updateHeightCm(value: String) {
        if (value.length <= 3 && value.all { it.isDigit() }) {
            _state.update { it.copy(heightCmText = value) }
        }
    }

    fun updateWeightKg(value: String) {
        if (value.length <= 3 && value.all { it.isDigit() }) {
            _state.update { it.copy(weightKgText = value) }
        }
    }

    fun selectActivity(value: ProfileActivityLevel) {
        _state.update { it.copy(activity = value) }
    }

    fun goBack() {
        _state.update { s ->
            if (s.stepIndex > 0) s.copy(stepIndex = s.stepIndex - 1) else s
        }
    }

    fun isCurrentStepValid(): Boolean = isStepValid(_state.value)

    fun onContinue(): ProfileOnboardingContinueResult {
        val s = _state.value
        if (!isStepValid(s)) return ProfileOnboardingContinueResult.Invalid
        if (s.stepIndex >= TOTAL_STEPS - 1) return ProfileOnboardingContinueResult.Finished
        _state.update { it.copy(stepIndex = it.stepIndex + 1) }
        return ProfileOnboardingContinueResult.Advanced
    }

    private fun isStepValid(s: ProfileOnboardingUiState): Boolean {
        return when (s.stepIndex) {
            0 -> s.name.trim().length >= 2
            1 -> {
                val age = s.ageText.toIntOrNull()
                age != null && age in 13..120
            }
            2 -> s.gender != null
            3 -> {
                val h = s.heightCmText.toIntOrNull()
                h != null && h in 100..250
            }
            4 -> {
                val w = s.weightKgText.toIntOrNull()
                w != null && w in 35..300
            }
            5 -> s.activity != null
            else -> false
        }
    }

    /**
     * Saves to Firestore when signed in; always caches the display name if the profile is complete
     * so Finish still proceeds when offline, Firestore rules fail, or staging auth has no Firebase user.
     */
    suspend fun finishOnboarding(): Boolean {
        val profile = _state.value.toUserProfile()
        if (!profile.isComplete()) {
            AppLogger.app(
                message = "finishOnboarding blocked — UserProfile not complete",
                extras = mapOf(
                    "name" to profile.name,
                    "age" to profile.age,
                    "gender" to profile.gender,
                    "heightCm" to profile.heightCm,
                    "weightKg" to profile.weightKg,
                    "activity" to profile.activity,
                ),
            )
            return false
        }
        val result = userProfileRepository.saveProfile(profile)
        if (result.isFailure) {
            displayNameStore.set(profile.name.trim())
            val err = result.exceptionOrNull()
            AppLogger.app(
                message = "profile_firestore_save_failed_using_local_name_cache",
                extras = mapOf("error" to (err?.message ?: "unknown")),
                throwable = err,
            )
        }
        return true
    }

    /** Runs finish on [viewModelScope] (avoid Compose scope issues); Logcat tag APP / FIREBASE. */
    fun submitFinishAndNavigate(onNavigateHome: () -> Unit) {
        viewModelScope.launch {
            AppLogger.app(message = "onboarding_finish_clicked → suspend pipeline start")
            val ok = finishOnboarding()
            AppLogger.app(message = "onboarding_finish_pipeline_done", extras = mapOf("willNavigate" to ok))
            if (ok) {
                onNavigateHome()
            }
        }
    }

    companion object {
        const val TOTAL_STEPS = 6
    }
}

private fun ProfileOnboardingUiState.withLoadedProfile(saved: UserProfile): ProfileOnboardingUiState {
    return copy(
        name = saved.name.ifBlank { name },
        ageText = saved.age?.toString() ?: ageText,
        gender = saved.gender?.let { runCatching { ProfileGender.valueOf(it) }.getOrNull() } ?: gender,
        heightCmText = saved.heightCm?.toString() ?: heightCmText,
        weightKgText = saved.weightKg?.toString() ?: weightKgText,
        activity = saved.activity?.let { runCatching { ProfileActivityLevel.valueOf(it) }.getOrNull() }
            ?: activity,
    )
}

private fun ProfileOnboardingUiState.toUserProfile(): UserProfile {
    return UserProfile(
        name = name.trim(),
        age = ageText.toIntOrNull(),
        gender = gender?.name,
        heightCm = heightCmText.toIntOrNull(),
        weightKg = weightKgText.toIntOrNull(),
        activity = activity?.name,
    )
}
