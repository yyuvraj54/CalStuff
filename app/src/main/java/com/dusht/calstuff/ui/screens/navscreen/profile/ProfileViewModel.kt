package com.dusht.calstuff.ui.screens.navscreen.profile

import com.dusht.calstuff.utils.base.BaseViewModel
import com.dusht.shared.session.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
) : BaseViewModel<ProfileUiState, ProfileEvent, ProfileEffect>(ProfileUiState()) {

    override fun handleEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LogoutClicked -> setState { copy(showLogoutDialog = true) }
            ProfileEvent.LogoutDismissed -> setState { copy(showLogoutDialog = false) }
            ProfileEvent.LogoutConfirmed -> performLogout()
        }
    }

    private fun performLogout() {
        setState { copy(showLogoutDialog = false) }
        userSessionRepository.setLoggedIn(false)
        sendEffect(ProfileEffect.NavigateToLogin)
    }
}
