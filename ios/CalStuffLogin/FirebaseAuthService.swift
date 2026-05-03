import Foundation
import UIKit
import FirebaseAuth
import FirebaseCore
import GoogleSignIn

/// Phone + Google sign-in using the same Firebase project as Android (`GoogleService-Info.plist`).
@MainActor
enum FirebaseAuthService {
    private static let stagingMockVerificationId = "staging_mock_vid"

    // MARK: - Phone

    static func sendPhoneVerificationCode(phoneDigits: String) async throws -> String {
        let digits = phoneDigits.filter(\.isNumber)
        guard !digits.isEmpty else {
            throw NSError(domain: "CalStuff", code: 1, userInfo: [NSLocalizedDescriptionKey: "Enter a phone number"])
        }

        if LoginConfig.isStaging && digits == LoginConfig.stagingPhoneDigits {
            return stagingMockVerificationId
        }

        let e164 = digitsToE164(digits)
        return try await withCheckedThrowingContinuation { (cont: CheckedContinuation<String, Error>) in
            PhoneAuthProvider.provider().verifyPhoneNumber(e164, uiDelegate: nil) { verificationID, error in
                if let error {
                    cont.resume(throwing: error)
                    return
                }
                guard let verificationID else {
                    cont.resume(throwing: NSError(
                        domain: "CalStuff",
                        code: 2,
                        userInfo: [NSLocalizedDescriptionKey: "Verification ID missing"]
                    ))
                    return
                }
                cont.resume(returning: verificationID)
            }
        }
    }

    static func signInWithSmsCode(verificationId: String, smsCode: String) async throws {
        let code = smsCode.trimmingCharacters(in: .whitespacesAndNewlines)

        if LoginConfig.isStaging && verificationId == stagingMockVerificationId {
            guard code == LoginConfig.stagingOtp else {
                throw NSError(
                    domain: "CalStuff",
                    code: 3,
                    userInfo: [NSLocalizedDescriptionKey: "Invalid code for staging test account"]
                )
            }
            return
        }

        let credential = PhoneAuthProvider.provider().credential(withVerificationID: verificationId, smsCode: code)
        _ = try await Auth.auth().signIn(with: credential)
    }

    // MARK: - Google

    static func signInWithGoogle(presenting viewController: UIViewController) async throws {
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            throw NSError(
                domain: "CalStuff",
                code: 4,
                userInfo: [NSLocalizedDescriptionKey: "Firebase client ID missing — add GoogleService-Info.plist"]
            )
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: viewController)
        guard let idToken = result.user.idToken?.tokenString else {
            throw NSError(domain: "CalStuff", code: 5, userInfo: [NSLocalizedDescriptionKey: "Google ID token missing"])
        }
        let accessToken = result.user.accessToken.tokenString
        let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: accessToken)
        _ = try await Auth.auth().signIn(with: credential)
    }

    static func handleGoogleURL(_ url: URL) -> Bool {
        GIDSignIn.sharedInstance.handle(url)
    }

    // MARK: - Helpers

    private static func digitsToE164(_ digits: String) -> String {
        let d = digits.filter(\.isNumber)
        if d.count == 12, d.hasPrefix("91") { return "+\(d)" }
        if d.count == 11, d.hasPrefix("1") { return "+\(d)" }
        if d.count == 10, let first = d.first, let v = first.wholeNumberValue, (6...9).contains(v) {
            return "+91\(d)"
        }
        if d.count == 10 { return "+1\(d)" }
        return "+\(d)"
    }
}

enum UIWindowSceneKeyWindow {
    static var rootViewController: UIViewController? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first { $0.isKeyWindow }?
            .rootViewController
    }
}
