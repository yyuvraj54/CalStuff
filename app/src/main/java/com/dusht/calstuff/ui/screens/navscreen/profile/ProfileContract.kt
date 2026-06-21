package com.dusht.calstuff.ui.screens.navscreen.profile

import com.dusht.calstuff.utils.base.ViewEffect
import com.dusht.calstuff.utils.base.ViewEvent
import com.dusht.calstuff.utils.base.ViewState

data class ProfileUiState(
    val userName: String = "",
    val memberSinceText: String = "New Account",
    val showLogoutDialog: Boolean = false,
) : ViewState

sealed interface ProfileEvent : ViewEvent {
    data object LogoutClicked : ProfileEvent
    data object LogoutConfirmed : ProfileEvent
    data object LogoutDismissed : ProfileEvent
}

sealed interface ProfileEffect : ViewEffect {
    data object NavigateToLogin : ProfileEffect
}
