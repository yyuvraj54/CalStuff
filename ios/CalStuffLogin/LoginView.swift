import SwiftUI
import UIKit

/// SwiftUI twin of Android `LoginScreen`: white background, hero image, phone → Continue → OTP → Verify, "or" + Google hidden while phone field has digits.
struct LoginView: View {
    @ObservedObject var viewModel: LoginViewModel
    var onGoogleSignIn: () -> Void
    var onPhoneContinue: () -> Void
    var onVerifyOtp: () -> Void

    init(
        viewModel: LoginViewModel,
        onGoogleSignIn: @escaping () -> Void = {},
        onPhoneContinue: @escaping () -> Void = {},
        onVerifyOtp: @escaping () -> Void = {}
    ) {
        self.viewModel = viewModel
        self.onGoogleSignIn = onGoogleSignIn
        self.onPhoneContinue = onPhoneContinue
        self.onVerifyOtp = onVerifyOtp
    }

    var body: some View {
        VStack(spacing: 0) {
            loginHero
            ScrollView {
                VStack(spacing: 16) {
                    if viewModel.isLoading {
                        ProgressView()
                            .padding(16)
                    }
                    loginFormContent
                }
                .padding(.horizontal, 32)
                .padding(.top, 48)
                .padding(.bottom, 64)
            }
        }
        .background(LoginDesignTokens.background)
        .alert(
            "CalStuff",
            isPresented: Binding(
                get: { viewModel.errorMessage != nil },
                set: { if !$0 { viewModel.consumeError() } }
            ),
            actions: {
                Button("OK") { viewModel.consumeError() }
            },
            message: {
                Text(viewModel.errorMessage ?? "")
            }
        )
    }

    private var loginHero: some View {
        Group {
            if let image = UIImage(named: "login_bg") {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
            } else {
                LinearGradient(
                    colors: [
                        Color(hex: 0xFFE3F2FD),
                        Color(hex: 0xFFF3E5F5)
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: 260)
        .clipped()
        .clipShape(
            RoundedRectangle(cornerRadius: 32, style: .continuous)
        )
        .padding(.horizontal, 0)
    }

    private var loginFormContent: some View {
        VStack(spacing: 16) {
            Text("Welcome Back")
                .font(.system(size: 32, weight: .bold))
                .foregroundStyle(LoginDesignTokens.title)
                .multilineTextAlignment(.center)

            Text("Kickstart your calorie diet with the best plan to reach your goals!")
                .font(.system(size: 14))
                .foregroundStyle(LoginDesignTokens.description)
                .multilineTextAlignment(.center)

            if LoginConfig.isStaging {
                Text("Staging: use \(LoginConfig.stagingPhoneDigits) · OTP \(LoginConfig.stagingOtp)")
                    .font(.caption)
                    .foregroundStyle(LoginDesignTokens.stagingAccent)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 8)
            }

            Spacer().frame(height: 4)

            phoneBlock

            if !viewModel.hasPhoneInput {
                VStack(spacing: 16) {
                    CalOrDivider(text: "or")
                    googleButton
                }
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .animation(.easeInOut(duration: 0.25), value: viewModel.hasPhoneInput)
    }

    private var phoneBlock: some View {
        VStack(spacing: 16) {
            CalAuraTextField(
                title: "Phone number",
                placeholder: "Phone number",
                text: Binding(
                    get: { viewModel.phoneDigits },
                    set: { viewModel.setPhoneDigits($0) }
                ),
                keyboard: .phonePad,
                textContentType: .telephoneNumber,
                enabled: viewModel.phonePhase == .phoneEntry
            )

            if viewModel.phonePhase == .phoneEntry, viewModel.canSendCode {
                Button(action: onPhoneContinue) {
                    Text("Continue")
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                }
                .buttonStyle(.plain)
                .foregroundStyle(.white)
                .background(LoginDesignTokens.continueButton)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            }

            if viewModel.phonePhase == .otpEntry {
                VStack(alignment: .leading, spacing: 12) {
                    Text("Enter the 6-digit code sent to your number.")
                        .font(.caption)
                        .foregroundStyle(LoginDesignTokens.otpHint)

                    CalAuraTextField(
                        title: "Verification code",
                        placeholder: "6-digit code",
                        text: Binding(
                            get: { viewModel.otpDigits },
                            set: { viewModel.setOtpDigits($0) }
                        ),
                        keyboard: .numberPad,
                        textContentType: .oneTimeCode,
                        enabled: true
                    )

                    if viewModel.otpReady {
                        Button(action: onVerifyOtp) {
                            Text("Verify & continue")
                                .fontWeight(.semibold)
                                .frame(maxWidth: .infinity)
                                .frame(height: 52)
                        }
                        .buttonStyle(.plain)
                        .foregroundStyle(.white)
                        .background(LoginDesignTokens.verifyButton)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    }
                }
                .padding(.top, 8)
            }
        }
    }

    private var googleButton: some View {
        Button(action: onGoogleSignIn) {
            HStack(spacing: 12) {
                if UIImage(named: "google_btn_icon") != nil {
                    Image("google_btn_icon")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 24, height: 24)
                } else {
                    Image(systemName: "g.circle.fill")
                        .font(.system(size: 24))
                        .foregroundStyle(.white)
                }
                Text("Sign in with Google")
                    .font(.system(size: 16, weight: .medium))
            }
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .foregroundStyle(.white)
            .background(LoginDesignTokens.googleButton)
            .clipShape(RoundedRectangle(cornerRadius: 28, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    LoginView(viewModel: LoginViewModel())
}
