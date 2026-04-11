import Foundation
import Combine

/// Mirrors Android `LoginUiState` / `PhoneLoginPhase` for the same UX flow (phone first, OTP, Google hides when typing).
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
        }
    }

    func setOtpDigits(_ raw: String) {
        otpDigits = String(raw.filter { $0.isNumber }.prefix(Self.otpLength))
    }

    /// Call after a successful Continue (code sent).
    func moveToOtpEntry() {
        phonePhase = .otpEntry
        otpDigits = ""
    }

    func consumeError() {
        errorMessage = nil
    }
}
