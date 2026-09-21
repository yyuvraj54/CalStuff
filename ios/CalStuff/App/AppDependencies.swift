import Foundation
import CalStuffShared

/// Composition root — constructed once in `CalStuffApp.swift`, threaded into view models via
/// initializers. Mirrors what Hilt's `DataBindsModule`/`DataProvidesModule` wire on Android;
/// there's no DI framework here since a single manually-built container is enough for one app.
final class AppDependencies {
    let sessionRepository: UserSessionRepository = UserSessionRepositoryImpl()
    let themePreferenceRepository: ThemePreferenceRepository = ThemePreferenceRepositoryImpl()
    let displayNameStore: DisplayNameStore = DisplayNameKeychainStore()
    lazy var userProfileRepository: UserProfileRepository = UserProfileFirestoreRepository(displayNameStore: displayNameStore)
    lazy var profileGateRepository: ProfileGateRepository = ProfileGateFirestoreRepository(userProfileRepository: userProfileRepository)
    let nutritionRepository: NutritionRepository = NutritionFirestoreRepository()
    let streakRepository: StreakRepository = StreakFirestoreRepository()
}
