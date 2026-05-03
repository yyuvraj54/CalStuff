import Foundation
import Combine

@MainActor
final class LoginViewModel: ObservableObject {
    enum PhonePhase {
        case phoneEntry
        case otpEntry
    }

    private static let phoneMaxDigits = 11
    private static let minDigitsContinue = 10
    private static let otpLength = 6

    @Published var phoneDigits: String = ""
    @Published var otpDigits: String = ""
    @Published var phonePhase: PhonePhase = .phoneEntry
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    @Published var didCompleteLogin: Bool = false

    private var verificationId: String?

    var hasPhoneInput: Bool {
        phoneDigits.contains { $0.isNumber }
    }

    var canSendCode: Bool {
        phoneDigits.filter { $0.isNumber }.count >= Self.minDigitsContinue
    }

    var otpReady: Bool {
        otpDigits.count == Self.otpLength
    }

    func setPhoneDigits(_ raw: String) {
        let filtered = String(raw.filter { $0.isNumber }.prefix(Self.phoneMaxDigits))
        let previous = phoneDigits.filter { $0.isNumber }
        let wasOtp = phonePhase == .otpEntry
        let phoneChangedWhileOtp = wasOtp && filtered != previous

        phoneDigits = filtered
        if filtered.isEmpty || phoneChangedWhileOtp {
            phonePhase = .phoneEntry
            otpDigits = ""
            verificationId = nil
        }
    }

    func setOtpDigits(_ raw: String) {
        otpDigits = String(raw.filter { $0.isNumber }.prefix(Self.otpLength))
    }

    func sendPhoneVerificationCode() async {
        guard canSendCode else { return }
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let vid = try await FirebaseAuthService.sendPhoneVerificationCode(phoneDigits: phoneDigits)
            verificationId = vid
            phonePhase = .otpEntry
            otpDigits = ""
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func verifySmsCode() async {
        guard let vid = verificationId, otpReady else { return }
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            try await FirebaseAuthService.signInWithSmsCode(verificationId: vid, smsCode: otpDigits)
            didCompleteLogin = true
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func signInWithGoogle() async {
        guard let root = UIWindowSceneKeyWindow.rootViewController else {
            errorMessage = "Could not find a view controller for Google sign-in."
            return
        }
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            try await FirebaseAuthService.signInWithGoogle(presenting: root)
            didCompleteLogin = true
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func consumeError() {
        errorMessage = nil
    }
}
