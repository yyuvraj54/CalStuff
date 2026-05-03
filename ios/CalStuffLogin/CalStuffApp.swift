//
//  CalStuffApp.swift
//  CalStuff
//

import SwiftUI
import FirebaseCore
import FirebaseAnalytics

@main
struct CalStuffApp: App {
    @StateObject private var loginViewModel = LoginViewModel()

    init() {
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
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
            .onOpenURL { url in
                _ = FirebaseAuthService.handleGoogleURL(url)
            }
        }
    }
}
