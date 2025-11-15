//package com.dusht.calstuff.vm
//
//import android.content.Context
//import android.content.Intent
//import androidx.activity.compose.ManagedActivityResultLauncher
//import androidx.activity.result.ActivityResult
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.dusht.calstuff.login.GoogleSignInUtils
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//sealed class LoginState {
//    object Idle : LoginState()
//    object Loading : LoginState()
//    data class Success(val userProfile: UserProfile) : LoginState()
//    data class Error(val message: String) : LoginState()
//}
//
//// MVI Intent
//sealed class LoginIntent {
//    data class GoogleSignIn(
//        val context: Context,
//        val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?
//    ) : LoginIntent()
//    object ResetState : LoginIntent()
//}
//
//// MVI Effect (One-time events)
//sealed class LoginEffect {
//    object NavigateToHome : LoginEffect()
//    data class ShowToast(val message: String) : LoginEffect()
//}
//
//@HiltViewModel
//class LoginViewModel @Inject constructor(
//    private val userRepository: UserRepository
//) : ViewModel() {
//
//    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
//    val state: StateFlow<LoginState> = _state.asStateFlow()
//
//    private val _effect = MutableStateFlow<LoginEffect?>(null)
//    val effect: StateFlow<LoginEffect?> = _effect.asStateFlow()
//
//    fun handleIntent(intent: LoginIntent) {
//        when (intent) {
//            is LoginIntent.GoogleSignIn -> {
//                performGoogleSignIn(intent.context, intent.launcher)
//            }
//            is LoginIntent.ResetState -> {
//                _state.value = LoginState.Idle
//            }
//        }
//    }
//
//    private fun performGoogleSignIn(
//        context: Context,
//        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?
//    ) {
//        _state.value = LoginState.Loading
//
//        GoogleSignInUtils.doGoogleSignIn(
//            context = context,
//            scope = viewModelScope,
//            launcher = launcher,
//            login = { userProfile ->
//                handleLoginSuccess(userProfile)
//            },
//            onError = { errorMessage ->
//                handleLoginError(errorMessage)
//            }
//        )
//    }
//
//    private fun handleLoginSuccess(userProfile: UserProfile) {
//        viewModelScope.launch {
//            try {
//                // Save user profile to local database/preferences
//                userRepository.saveUserProfile(userProfile)
//
//                _state.value = LoginState.Success(userProfile)
//                _effect.value = LoginEffect.NavigateToHome
//                _effect.value = LoginEffect.ShowToast("Welcome ${userProfile.displayName}!")
//            } catch (e: Exception) {
//                _state.value = LoginState.Error("Failed to save user data: ${e.message}")
//            }
//        }
//    }
//
//    private fun handleLoginError(message: String) {
//        _state.value = LoginState.Error(message)
//        _effect.value = LoginEffect.ShowToast(message)
//    }
//
//    fun clearEffect() {
//        _effect.value = null
//    }
//}