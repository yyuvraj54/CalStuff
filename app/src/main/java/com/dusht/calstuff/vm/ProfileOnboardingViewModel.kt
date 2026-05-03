package com.dusht.calstuff.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
class ProfileOnboardingViewModel @Inject constructor() : ViewModel() {

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

    companion object {
        const val TOTAL_STEPS = 6
    }
}
