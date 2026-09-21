import Foundation
import CalStuffShared

/// Mirrors Android's `ProfileGateRepositoryImpl` exactly — both platforms delegate the actual
/// "is this profile complete" rule to the shared `isProfileComplete(_:)` function so there's a
/// single source of truth for that decision.
final class ProfileGateFirestoreRepository: ProfileGateRepository {
    private let userProfileRepository: UserProfileRepository

    init(userProfileRepository: UserProfileRepository) {
        self.userProfileRepository = userProfileRepository
    }

    func fetchProfileCompleteness() async throws -> ProfileCompleteness {
        let profile = try await userProfileRepository.getProfile()
        return ProfileCompleteness(isProfileComplete: ProfileGateRepositoryKt.isProfileComplete(profile: profile))
    }
}
