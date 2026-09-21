import SwiftUI
import CalStuffShared

/// Wraps the existing `LoginView`/`LoginViewModel` (from `CalStuffLogin/`, untouched) and reacts
/// to `didCompleteLogin` by persisting the session and asking the caller to re-route — keeps the
/// login screen itself platform-auth-only, with session wiring layered on top here.
struct LoginContainerView: View {
    @StateObject private var loginViewModel = LoginViewModel()
    let dependencies: AppDependencies
    /// Called after the session is persisted — expected to run the post-login gate
    /// (`RootViewModel.handlePostLogin`), not just a plain route refresh.
    let onLoggedIn: () -> Void

    var body: some View {
        LoginView(
            viewModel: loginViewModel,
            onGoogleSignIn: {
                Task { await loginViewModel.signInWithGoogle() }
            },
            onPhoneContinue: {
                Task { await loginViewModel.sendPhoneVerificationCode() }
            },
            onVerifyOtp: {
                Task { await loginViewModel.verifySmsCode() }
            }
        )
        .onChange(of: loginViewModel.didCompleteLogin) { completed in
            guard completed else { return }
            dependencies.sessionRepository.setLoggedIn(value: true)
            onLoggedIn()
        }
    }
}
