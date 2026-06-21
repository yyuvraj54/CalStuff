package com.dusht.calstuff.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dusht.shared.session.DisplayNameStore
import com.dusht.shared.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val displayNameStore: DisplayNameStore,
) : ViewModel() {

    private val _displayName = MutableStateFlow(displayNameStore.get().orEmpty())
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            profile?.name?.trim()?.takeIf { it.isNotEmpty() }?.let { displayNameStore.set(it) }
            _displayName.value = displayNameStore.get().orEmpty()
        }
    }
}
