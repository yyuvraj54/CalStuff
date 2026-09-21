import Foundation
import Security
import CalStuffShared

/// Keychain-backed cache of the profile display name — mirrors Android's
/// `EncryptedSharedPreferences`-backed `DisplayNameStoreImpl` (secure storage, trimmed string,
/// `nil` when blank/absent). Implemented in Swift rather than Kotlin `iosMain`: unlike
/// `UserSessionRepositoryImpl`/`ThemePreferenceRepositoryImpl`, this needs Keychain (Security
/// framework), which Swift talks to far more directly than Kotlin/Native cinterop would.
final class DisplayNameKeychainStore: DisplayNameStore {
    private let service = "com.dusht.calstuff.displayName"
    private let account = "display_name"

    func get() -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne,
        ]
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        guard status == errSecSuccess,
              let data = result as? Data,
              let value = String(data: data, encoding: .utf8),
              !value.isEmpty else {
            return nil
        }
        return value
    }

    func set(name: String) {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        let data = Data(trimmed.utf8)
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
        ]

        if SecItemCopyMatching(query as CFDictionary, nil) == errSecSuccess {
            SecItemUpdate(query as CFDictionary, [kSecValueData as String: data] as CFDictionary)
        } else {
            var newItem = query
            newItem[kSecValueData as String] = data
            SecItemAdd(newItem as CFDictionary, nil)
        }
    }

    func clear() {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
        ]
        SecItemDelete(query as CFDictionary)
    }
}
