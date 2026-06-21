package com.dusht.calstuff.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dusht.shared.model.UserProfile
import com.dusht.shared.repository.UserProfileRepository
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
    val isSaving: Boolean = false,
    val saveError: String? = null,
)

sealed interface ProfileOnboardingContinueResult {
    data object Advanced : ProfileOnboardingContinueResult
    data object Finished : ProfileOnboardingContinueResult
    data object Invalid : ProfileOnboardingContinueResult
}

@HiltViewModel
class ProfileOnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileOnboardingUiState())
    val state: StateFlow<ProfileOnboardingUiState> = _state.asStateFlow()

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

    /**
     * Persists the completed profile to Firestore.
     * Call this when [onContinue] returns [ProfileOnboardingContinueResult.Finished].
     * Calls [onSuccess] on the main thread when done, or sets [ProfileOnboardingUiState.saveError].
     */
    fun saveProfile(onSuccess: () -> Unit) {
        val s = _state.value
        val uid = userProfileRepository.currentUserId() ?: run {
            _state.update { it.copy(saveError = "Not signed in. Please log in again.") }
            return
        }
        _state.update { it.copy(isSaving = true, saveError = null) }
        val now = System.currentTimeMillis()
        val profile = UserProfile(
            uid = uid,
            name = s.name.trim(),
            age = s.ageText.toIntOrNull() ?: 0,
            heightCm = s.heightCmText.toFloatOrNull() ?: 0f,
            weightKg = s.weightKgText.toFloatOrNull() ?: 0f,
            gender = s.gender?.name ?: "PREFER_NOT_SAY",
            activityLevel = s.activity?.name ?: "SEDENTARY",
            dailyCalorieGoal = computeCalorieGoal(s),
            createdAt = now,
            updatedAt = now,
        )
        viewModelScope.launch {
            userProfileRepository.saveProfile(profile)
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure { err ->
                    _state.update { it.copy(isSaving = false, saveError = err.message ?: "Save failed") }
                }
        }
    }

    fun clearSaveError() {
        _state.update { it.copy(saveError = null) }
    }

    private fun isStepValid(s: ProfileOnboardingUiState): Boolean {
        return when (s.stepIndex) {
            0 -> s.name.trim().length >= 2
            1 -> { val age = s.ageText.toIntOrNull(); age != null && age in 13..120 }
            2 -> s.gender != null
            3 -> { val h = s.heightCmText.toIntOrNull(); h != null && h in 100..250 }
            4 -> { val w = s.weightKgText.toIntOrNull(); w != null && w in 35..300 }
            5 -> s.activity != null
            else -> false
        }
    }

    /**
     * Mifflin–St Jeor equation for TDEE (Total Daily Energy Expenditure).
     * Returns a reasonable default (2000 kcal) if inputs are missing.
     */
    private fun computeCalorieGoal(s: ProfileOnboardingUiState): Int {
        val weight = s.weightKgText.toFloatOrNull() ?: return 2000
        val height = s.heightCmText.toFloatOrNull() ?: return 2000
        val age = s.ageText.toIntOrNull() ?: return 2000
        val bmr = if (s.gender == ProfileGender.MALE) {
            10f * weight + 6.25f * height - 5f * age + 5f
        } else {
            10f * weight + 6.25f * height - 5f * age - 161f
        }
        val multiplier = when (s.activity) {
            ProfileActivityLevel.SEDENTARY  -> 1.2f
            ProfileActivityLevel.LIGHT      -> 1.375f
            ProfileActivityLevel.MODERATE   -> 1.55f
            ProfileActivityLevel.ACTIVE     -> 1.725f
            ProfileActivityLevel.VERY_ACTIVE -> 1.9f
            null                            -> 1.2f
        }
        return (bmr * multiplier).toInt().coerceIn(1200, 5000)
    }

    companion object {
        const val TOTAL_STEPS = 6
    }
}
