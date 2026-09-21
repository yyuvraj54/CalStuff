import Foundation

/// Mirrors Android's sealed `AppRoute` — the set of top-level destinations this app can show.
/// Filled in screen-by-screen through Phases 4-6; unbuilt cases render a placeholder for now.
enum AppRoute: Hashable {
    case login
    case onboardingForm
    case home
    case meals
    case logs
    case profile
    case addMeal(dayOfMonth: Int)
    case settings
}
