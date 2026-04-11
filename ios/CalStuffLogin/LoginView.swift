import SwiftUI
import UIKit

/// SwiftUI login matching Android `LoginScreen`: flexible hero (`weight(1f)`), form below, bottom-rounded hero.
struct LoginView: View {
    @ObservedObject var viewModel: LoginViewModel
    var onGoogleSignIn: () -> Void
    var onPhoneContinue: () -> Void
    var onVerifyOtp: () -> Void

    @FocusState private var focusedField: LoginTextFocus?

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

    private static let heroBottomRadius: CGFloat = 32

    private enum ScrollAnchor {
        static let otpBlock = "otpBlock"
        static let verifyButton = "verifyButton"
    }

    var body: some View {
        GeometryReader { geo in
            let contentWidth = geo.size.width
            VStack(spacing: 0) {
                loginHero(width: contentWidth)
                    .frame(maxHeight: .infinity)

                if viewModel.isLoading {
                    ProgressView()
                        .padding(16)
                }

                ScrollViewReader { proxy in
                    ScrollView {
                        loginFormContent
                            .frame(maxWidth: .infinity)
                            .padding(.horizontal, 32)
                            .padding(.top, 48)
                            .padding(.bottom, 32)
                    }
                    .frame(maxHeight: .infinity)
                    .scrollDismissesKeyboard(.interactively)
                    .onChange(of: viewModel.phonePhase) { _, newPhase in
                        if newPhase == .otpEntry {
                            focusedField = .otp
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) {
                                withAnimation(.easeOut(duration: 0.25)) {
                                    proxy.scrollTo(ScrollAnchor.otpBlock, anchor: .center)
                                }
                            }
                        }
                    }
                    .onChange(of: viewModel.otpReady) { _, ready in
                        if ready {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                                withAnimation(.easeOut(duration: 0.25)) {
                                    proxy.scrollTo(ScrollAnchor.verifyButton, anchor: .bottom)
                                }
                            }
                        }
                    }
                }
            }
            .frame(width: contentWidth, height: geo.size.height, alignment: .top)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(LoginDesignTokens.background)
        .keyboardDoneButton()
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

    private func loginHero(width: CGFloat) -> some View {
        ZStack {
            if UIImage(named: "login_bg") != nil {
                Image("login_bg")
                    .resizable()
                    .scaledToFill()
                    .frame(width: width)
                    .frame(maxHeight: .infinity)
                    .clipped()
            } else {
                heroGradientFallback
            }
        }
        .frame(width: width)
        .frame(maxHeight: .infinity)
        .clipped()
        .clipShape(
            UnevenRoundedRectangle(
                topLeadingRadius: 0,
                bottomLeadingRadius: Self.heroBottomRadius,
                bottomTrailingRadius: Self.heroBottomRadius,
                topTrailingRadius: 0,
                style: .continuous
            )
        )
        .ignoresSafeArea(edges: .top)
    }

    private var heroGradientFallback: some View {
        LinearGradient(
            colors: [
                Color(hex: 0xFFE3F2FD),
                Color(hex: 0xFFF3E5F5)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
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
            CalAuraOutlinedTextField(
                title: "Phone number",
                placeholder: "Phone number",
                text: Binding(
                    get: { viewModel.phoneDigits },
                    set: { viewModel.setPhoneDigits($0) }
                ),
                keyboard: .phonePad,
                textContentType: .telephoneNumber,
                enabled: viewModel.phonePhase == .phoneEntry,
                focusTag: .phone,
                focusedField: $focusedField
            )

            if viewModel.phonePhase == .phoneEntry, viewModel.canSendCode {
                Button(action: {
                    KeyboardDismiss.endEditing()
                    onPhoneContinue()
                }) {
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

                    CalAuraOutlinedTextField(
                        title: "Verification code",
                        placeholder: "6-digit code",
                        text: Binding(
                            get: { viewModel.otpDigits },
                            set: { viewModel.setOtpDigits($0) }
                        ),
                        keyboard: .numberPad,
                        textContentType: .oneTimeCode,
                        enabled: true,
                        focusTag: .otp,
                        focusedField: $focusedField
                    )

                    if viewModel.otpReady {
                        Button(action: {
                            KeyboardDismiss.endEditing()
                            onVerifyOtp()
                        }) {
                            Text("Verify & continue")
                                .fontWeight(.semibold)
                                .frame(maxWidth: .infinity)
                                .frame(height: 52)
                        }
                        .buttonStyle(.plain)
                        .foregroundStyle(.white)
                        .background(LoginDesignTokens.verifyButton)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .id(ScrollAnchor.verifyButton)
                    }
                }
                .padding(.top, 8)
                .id(ScrollAnchor.otpBlock)
            }
        }
    }

    private var googleButton: some View {
        Button(action: {
            KeyboardDismiss.endEditing()
            onGoogleSignIn()
        }) {
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
