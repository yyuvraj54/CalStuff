import Foundation
import Combine
import CalStuffShared

/// Drives `RootView`'s destination + theme, mirroring Android's `getStartDestination` (in
/// `AppNavGraph.kt`) and the `ThemeViewModel`/`CalStuffTheme` wiring at the Activity root.
@MainActor
final class RootViewModel: ObservableObject {
    @Published private(set) var route: AppRoute
    @Published private(set) var themeMode: ThemeMode
    /// True while the post-login profile-completeness gate is resolving — mirrors Android's
    /// `PostLoginScreen`, which shows a full-screen loading animation during this same gap.
    @Published private(set) var isResolvingPostLogin = false

    let dependencies: AppDependencies
    private var themeSubscription: FlowSubscription?

    init(dependencies: AppDependencies) {
        self.dependencies = dependencies
        self.route = Self.resolveRoute(dependencies: dependencies)
        self.themeMode = (dependencies.themePreferenceRepository.themeMode.value as? ThemeMode) ?? .system

        themeSubscription = FlowBridgeKt.watch(dependencies.themePreferenceRepository.themeMode) { [weak self] value in
            guard let mode = value as? ThemeMode else { return }
            self?.themeMode = mode
        }
    }

    /// Call after onboarding finishes or logout — re-derives the route from current session
    /// state, same as Android re-evaluating `getStartDestination` on relaunch.
    func refresh() {
        route = Self.resolveRoute(dependencies: dependencies)
    }

    /// Call right after a fresh login. Mirrors Android's `PostLoginScreen`/`PostLoginViewModel`:
    /// does a real Firestore-backed profile-completeness check (not just the local
    /// `hasCompletedOnboarding` flag) so a reinstall with an already-complete cloud profile skips
    /// onboarding instead of asking again.
    func handlePostLogin() {
        isResolvingPostLogin = true
        Task {
            do {
                let completeness = try await dependencies.profileGateRepository.fetchProfileCompleteness()
                if completeness.isProfileComplete {
                    dependencies.sessionRepository.setOnboardingCompleted(value: true)
                }
            } catch {
                AppLog.error("RootViewModel.handlePostLogin", error)
            }
            refresh()
            isResolvingPostLogin = false
        }
    }

    func setThemeMode(_ mode: ThemeMode) {
        dependencies.themePreferenceRepository.setThemeMode(mode: mode)
    }

    private static func resolveRoute(dependencies: AppDependencies) -> AppRoute {
        guard dependencies.sessionRepository.isLoggedIn() else { return .login }
        guard dependencies.sessionRepository.hasCompletedOnboarding() else { return .onboardingForm }
        return .home
    }

    deinit {
        themeSubscription?.cancel()
    }
}
