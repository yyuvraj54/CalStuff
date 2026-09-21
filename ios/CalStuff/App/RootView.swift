import SwiftUI
import CalStuffShared

/// Navigation root — mirrors Android's `AppNavGraph`. Resolves which top-level screen to show
/// from session state, and applies the resolved theme to everything beneath it (mirrors
/// `CalStuffTheme(darkTheme:)` wrapping `CalStuffApp()` on Android).
struct RootView: View {
    @StateObject private var viewModel: RootViewModel
    @Environment(\.colorScheme) private var systemColorScheme

    init(dependencies: AppDependencies) {
        _viewModel = StateObject(wrappedValue: RootViewModel(dependencies: dependencies))
    }

    private var darkTheme: Bool {
        switch viewModel.themeMode {
        case .light: return false
        case .dark: return true
        default: return systemColorScheme == .dark // .system, or any future case
        }
    }

    var body: some View {
        Group {
            if viewModel.isResolvingPostLogin {
                PostLoginLoadingView()
            } else {
                switch viewModel.route {
                case .login:
                    LoginContainerView(dependencies: viewModel.dependencies, onLoggedIn: viewModel.handlePostLogin)
                case .onboardingForm:
                    OnboardingView(dependencies: viewModel.dependencies, onFinished: {
                        viewModel.dependencies.sessionRepository.setOnboardingCompleted(value: true)
                        viewModel.refresh()
                    })
                case .home, .meals, .logs, .profile:
                    // All four are bottom-nav tabs sharing one screen (mirrors Android's
                    // BottomNavDestination), not independent top-level routes — resolveRoute only
                    // ever produces .home, but MainTabView itself picks the tab.
                    MainTabView(dependencies: viewModel.dependencies, rootViewModel: viewModel, onLoggedOut: viewModel.refresh)
                case .addMeal:
                    PlaceholderScreen(title: "Add Meal", note: "Reached via Home/Meals' + button, not this route")
                case .settings:
                    PlaceholderScreen(title: "Settings", note: "Not built on Android either — stub only")
                }
            }
        }
        .calStuffTheme(darkTheme: darkTheme)
    }
}

/// Shown while `handlePostLogin()` resolves the profile-completeness gate — mirrors Android's
/// `PostLoginScreen`'s full-screen `LoadingAnimation()` (a Lottie walking-avocado there; no such
/// asset exists in the iOS project, so a native `ProgressView` is the honest substitution).
private struct PostLoginLoadingView: View {
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()
            ProgressView().tint(colors.accent)
        }
    }
}

/// Temporary stand-in for not-yet-built screens — also doubles as a visible proof that the
/// design-system environment (`\.calStuffColors`) is wired correctly end to end.
private struct PlaceholderScreen: View {
    let title: String
    let note: String
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()
            VStack(spacing: 8) {
                Text(title)
                    .font(.title.bold())
                    .foregroundStyle(colors.textPrimary)
                Text(note)
                    .font(.subheadline)
                    .foregroundStyle(colors.textSecondary)
            }
        }
    }
}

#Preview {
    RootView(dependencies: AppDependencies())
}
