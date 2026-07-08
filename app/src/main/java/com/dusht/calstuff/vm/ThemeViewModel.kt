package com.dusht.calstuff.vm

import androidx.lifecycle.ViewModel
import com.dusht.shared.session.ThemeMode
import com.dusht.shared.session.ThemePreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferenceRepository: ThemePreferenceRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferenceRepository.themeMode

    fun setThemeMode(mode: ThemeMode) {
        themePreferenceRepository.setThemeMode(mode)
    }
}
