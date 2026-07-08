package com.dusht.calstuff.ui.screens.navscreen.profile

import com.dusht.calstuff.utils.base.BaseViewModel
import com.dusht.shared.repository.UserProfileRepository
import com.dusht.shared.session.DisplayNameStore
import com.dusht.shared.session.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val displayNameStore: DisplayNameStore,
) : BaseViewModel<ProfileUiState, ProfileEvent, ProfileEffect>(
    ProfileUiState(userName = displayNameStore.get().orEmpty())
) {

    init {
        loadProfile()
    }

    override fun handleEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LogoutClicked -> setState { copy(showLogoutDialog = true) }
            ProfileEvent.LogoutDismissed -> setState { copy(showLogoutDialog = false) }
            ProfileEvent.LogoutConfirmed -> performLogout()
        }
    }

    private fun loadProfile() {
        launchIO {
            val profile = userProfileRepository.getProfile() ?: return@launchIO
            setState {
                copy(
                    userName = profile.name.ifBlank { userName },
                    memberSinceText = formatMemberSince(profile.createdAt),
                )
            }
        }
    }

    private fun formatMemberSince(createdAtMs: Long): String {
        if (createdAtMs <= 0L) return "New Account"
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return formatter.format(createdAtMs)
    }

    private fun performLogout() {
        setState { copy(showLogoutDialog = false) }
        userSessionRepository.setLoggedIn(false)
        sendEffect(ProfileEffect.NavigateToLogin)
    }
}
