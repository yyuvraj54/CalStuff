package com.dusht.calstuff.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dusht.shared.profile.ProfileGateRepository
import com.dusht.shared.session.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PostLoginDestination {
    data object ProfileOnboarding : PostLoginDestination
    data object Home : PostLoginDestination
}

@HiltViewModel
class PostLoginViewModel @Inject constructor(
    private val profileGateRepository: ProfileGateRepository,
    private val userSessionRepository: UserSessionRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<PostLoginDestination?>(null)
    val destination: StateFlow<PostLoginDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val completeness = profileGateRepository.fetchProfileCompleteness()
            if (completeness.isProfileComplete) {
                userSessionRepository.setOnboardingCompleted(true)
                _destination.value = PostLoginDestination.Home
            } else {
                _destination.value = PostLoginDestination.ProfileOnboarding
            }
        }
    }

    fun consumeDestination() {
        _destination.value = null
    }
}
