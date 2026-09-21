import Foundation
import FirebaseAuth
import CalStuffShared

/// Mirrors Android's `ProfileViewModel` (userName/memberSinceText/showLogoutDialog + logout
/// flow) — the Theme row lives in `RootViewModel`/`AppDependencies.themePreferenceRepository`
/// directly rather than being duplicated here, same separation as the Android version.
@MainActor
final class ProfileViewModel: ObservableObject {
    @Published var userName = ""
    @Published var memberSinceText = "New Account"
    @Published var showLogoutDialog = false

    private let dependencies: AppDependencies

    init(dependencies: AppDependencies) {
        self.dependencies = dependencies
        self.userName = dependencies.displayNameStore.get() ?? ""
        Task { await loadProfile() }
    }

    private func loadProfile() async {
        guard let profile = try? await dependencies.userProfileRepository.getProfile() else { return }
        if !profile.name.isEmpty {
            userName = profile.name
        }
        memberSinceText = Self.formatMemberSince(profile.createdAt)
    }

    /// Mirrors Android's `ProfileViewModel.formatMemberSince` exactly.
    private static func formatMemberSince(_ createdAtMs: Int64) -> String {
        guard createdAtMs > 0 else { return "New Account" }
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM yyyy"
        return formatter.string(from: Date(timeIntervalSince1970: Double(createdAtMs) / 1000))
    }

    func logoutTapped() {
        showLogoutDialog = true
    }

    func logoutDismissed() {
        showLogoutDialog = false
    }

    func logoutConfirmed(onLoggedOut: @escaping () -> Void) {
        showLogoutDialog = false
        try? Auth.auth().signOut()
        dependencies.displayNameStore.clear()
        dependencies.sessionRepository.setLoggedIn(value: false)
        onLoggedOut()
    }
}
