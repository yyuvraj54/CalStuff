package com.dusht.calstuff.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.R
import com.dusht.calstuff.ui.common.CalAuraOutlinedTextField
import com.dusht.calstuff.ui.common.onboarding.OnboardingShellLayout
import com.dusht.calstuff.ui.common.onboarding.PrimaryStickyButton
import com.dusht.calstuff.vm.MainViewModel
import com.dusht.calstuff.vm.ProfileActivityLevel
import com.dusht.calstuff.vm.ProfileGender
import com.dusht.calstuff.vm.ProfileOnboardingContinueResult
import com.dusht.calstuff.vm.ProfileOnboardingViewModel

@Composable
fun OnboardingFormScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    profileVm: ProfileOnboardingViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val state by profileVm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show Firestore save errors in a snackbar.
    LaunchedEffect(state.saveError) {
        val err = state.saveError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        profileVm.clearSaveError()
    }

    val isLastStep = state.stepIndex == ProfileOnboardingViewModel.TOTAL_STEPS - 1
    val continueLabel = if (isLastStep) {
        stringResource(R.string.profile_finish)
    } else {
        stringResource(R.string.profile_continue)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Box(contentAlignment = Alignment.Center) {
                    PrimaryStickyButton(
                        text = continueLabel,
                        enabled = profileVm.isCurrentStepValid() && !state.isSaving,
                        onClick = {
                            when (profileVm.onContinue()) {
                                ProfileOnboardingContinueResult.Finished -> {
                                    // Save profile to Firestore; navigate only on success.
                                    profileVm.saveProfile {
                                        mainViewModel.completeOnboarding()
                                        onComplete()
                                    }
                                }
                                ProfileOnboardingContinueResult.Advanced,
                                ProfileOnboardingContinueResult.Invalid -> Unit
                            }
                        },
                    )
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        OnboardingShellLayout(
            title = stepTitleFor(state.stepIndex),
            currentStep = state.stepIndex,
            totalSteps = ProfileOnboardingViewModel.TOTAL_STEPS,
            showBack = state.stepIndex > 0 && !state.isSaving,
            onBack = profileVm::goBack,
            modifier = Modifier.padding(innerPadding),
        ) {
            when (state.stepIndex) {
                0 -> NameStep(value = state.name, onValueChange = profileVm::updateName)
                1 -> AgeStep(value = state.ageText, onValueChange = profileVm::updateAge)
                2 -> GenderStep(selected = state.gender, onSelect = profileVm::selectGender)
                3 -> HeightStep(value = state.heightCmText, onValueChange = profileVm::updateHeightCm)
                4 -> WeightStep(value = state.weightKgText, onValueChange = profileVm::updateWeightKg)
                5 -> ActivityStep(selected = state.activity, onSelect = profileVm::selectActivity)
            }
        }
    }
}

@Composable
private fun stepTitleFor(stepIndex: Int): String {
    return when (stepIndex) {
        0 -> stringResource(R.string.profile_step_name_title)
        1 -> stringResource(R.string.profile_step_age_title)
        2 -> stringResource(R.string.profile_step_gender_title)
        3 -> stringResource(R.string.profile_step_height_title)
        4 -> stringResource(R.string.profile_step_weight_title)
        5 -> stringResource(R.string.profile_step_activity_title)
        else -> ""
    }
}

@Composable
private fun NameStep(value: String, onValueChange: (String) -> Unit) {
    CalAuraOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.profile_name_placeholder),
        placeholder = stringResource(R.string.profile_name_placeholder),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )
}

@Composable
private fun AgeStep(value: String, onValueChange: (String) -> Unit) {
    CalAuraOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.profile_age_placeholder),
        placeholder = stringResource(R.string.profile_age_placeholder),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun GenderStep(selected: ProfileGender?, onSelect: (ProfileGender) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileGender.entries.forEach { g ->
            val label = when (g) {
                ProfileGender.MALE -> stringResource(R.string.profile_gender_male)
                ProfileGender.FEMALE -> stringResource(R.string.profile_gender_female)
                ProfileGender.OTHER -> stringResource(R.string.profile_gender_other)
                ProfileGender.PREFER_NOT_SAY -> stringResource(R.string.profile_gender_prefer_not)
            }
            FilterChip(
                selected = selected == g,
                onClick = { onSelect(g) },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HeightStep(value: String, onValueChange: (String) -> Unit) {
    CalAuraOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.profile_step_height_suffix),
        placeholder = stringResource(R.string.profile_step_height_suffix),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun WeightStep(value: String, onValueChange: (String) -> Unit) {
    CalAuraOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.profile_step_weight_suffix),
        placeholder = stringResource(R.string.profile_step_weight_suffix),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun ActivityStep(selected: ProfileActivityLevel?, onSelect: (ProfileActivityLevel) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileActivityLevel.entries.forEach { level ->
            val label = when (level) {
                ProfileActivityLevel.SEDENTARY -> stringResource(R.string.profile_activity_sedentary)
                ProfileActivityLevel.LIGHT -> stringResource(R.string.profile_activity_light)
                ProfileActivityLevel.MODERATE -> stringResource(R.string.profile_activity_moderate)
                ProfileActivityLevel.ACTIVE -> stringResource(R.string.profile_activity_active)
                ProfileActivityLevel.VERY_ACTIVE -> stringResource(R.string.profile_activity_very_active)
            }
            FilterChip(
                selected = selected == level,
                onClick = { onSelect(level) },
                label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
