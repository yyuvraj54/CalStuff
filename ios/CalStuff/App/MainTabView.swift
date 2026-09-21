import SwiftUI
import CalStuffShared

/// Bottom nav — Home/Meals/Logs as tabs sharing one `NutritionStore` subscription. Profile is
/// deliberately not a tab: it's reached via the avatar button in Home's top-right corner (pushed
/// on top, mirroring the Android home-screen-avatar-tap pattern) per explicit product direction.
struct MainTabView: View {
    @StateObject private var store: NutritionStore
    @ObservedObject var rootViewModel: RootViewModel
    let onLoggedOut: () -> Void

    init(dependencies: AppDependencies, rootViewModel: RootViewModel, onLoggedOut: @escaping () -> Void) {
        _store = StateObject(wrappedValue: NutritionStore(dependencies: dependencies))
        self.rootViewModel = rootViewModel
        self.onLoggedOut = onLoggedOut
    }

    var body: some View {
        TabView {
            HomeView(store: store, rootViewModel: rootViewModel, onLoggedOut: onLoggedOut)
                .tabItem { Label("Home", systemImage: "house.fill") }
            MealsView(store: store)
                .tabItem { Label("Meals", systemImage: "fork.knife") }
            LogsView(store: store)
                .tabItem { Label("Logs", systemImage: "list.bullet.rectangle") }
        }
    }
}
