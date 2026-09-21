import Foundation
import CalStuffShared

/// Mirrors Android's `ProfileOnboardingViewModel` — same 6 steps, same validation bounds, same
/// calorie-goal computation (delegated to the shared `CalorieGoalCalculator`, not reimplemented).
@MainActor
final class OnboardingViewModel: ObservableObject {
    static let totalSteps = 6

    @Published var stepIndex = 0
    @Published var name = ""
    @Published var ageText = ""
    @Published var gender: Gender?
    @Published var heightCmText = ""
    @Published var weightKgText = ""
    @Published var activity: ActivityLevel?
    @Published var isSaving = false
    @Published var saveError: String?

    private let userProfileRepository: UserProfileRepository

    init(userProfileRepository: UserProfileRepository) {
        self.userProfileRepository = userProfileRepository
    }

    var isCurrentStepValid: Bool {
        switch stepIndex {
        case 0: return name.trimmingCharacters(in: .whitespaces).count >= 2
        case 1: return (Int(ageText)).map { (13...120).contains($0) } ?? false
        case 2: return gender != nil
        case 3: return (Int(heightCmText)).map { (100...250).contains($0) } ?? false
        case 4: return (Int(weightKgText)).map { (35...300).contains($0) } ?? false
        case 5: return activity != nil
        default: return false
        }
    }

    func goBack() {
        if stepIndex > 0 { stepIndex -= 1 }
    }

    enum ContinueResult {
        case advanced
        case finished
        case invalid
    }

    func onContinue() -> ContinueResult {
        guard isCurrentStepValid else { return .invalid }
        if stepIndex >= Self.totalSteps - 1 { return .finished }
        stepIndex += 1
        return .advanced
    }

    func clearSaveError() {
        saveError = nil
    }

    /// Call when `onContinue()` returns `.finished`. Mirrors Android's `saveProfile(onSuccess:)`.
    func saveProfile(onSuccess: @escaping () -> Void) {
        guard let uid = userProfileRepository.currentUserId() else {
            saveError = "Not signed in. Please log in again."
            return
        }
        isSaving = true
        saveError = nil
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let calorieGoal = CalorieGoalCalculator.shared.compute(
            gender: gender,
            ageYears: Int(ageText).map { KotlinInt(int: Int32($0)) },
            heightCm: Float(heightCmText).map { KotlinFloat(float: $0) },
            weightKg: Float(weightKgText).map { KotlinFloat(float: $0) },
            activityLevel: activity
        )
        let profile = UserProfile(
            uid: uid,
            name: name.trimmingCharacters(in: .whitespaces),
            age: Int32(ageText) ?? 0,
            heightCm: Float(heightCmText) ?? 0,
            weightKg: Float(weightKgText) ?? 0,
            gender: (gender ?? .preferNotSay).wireValue,
            activityLevel: (activity ?? .sedentary).wireValue,
            dailyCalorieGoal: calorieGoal,
            createdAt: now,
            updatedAt: now,
            phoneNumber: userProfileRepository.currentPhoneNumber() ?? ""
        )
        Task {
            do {
                _ = try await userProfileRepository.saveProfile(profile: profile)
                isSaving = false
                onSuccess()
            } catch {
                isSaving = false
                saveError = error.localizedDescription
            }
        }
    }
}
