import Foundation
import CalStuffShared

/// In-memory cart item, built up before any Firestore write — mirrors Android's `FoodItem` data
/// class in `AddMealScreen.kt` exactly (same fields; `id` here is a purely local SwiftUI
/// identity, not persisted — Android has no id on `FoodItem` either, using list index instead).
struct FoodItem: Identifiable, Equatable {
    var id = UUID()
    var name: String
    var calories: Int32
    var protein: Float
    var carbs: Float
    var fat: Float
    var mealType: MealType = .snacks
}

/// Mirrors Android's `AddMealScreen` composable's local state + `parseMockItems` + the
/// `AppNavGraph.kt` save handler that iterates `items` — NOT the shape of the old single-item
/// iOS form this replaces. See ios/CHANGES.md for why: Android's real Add Meal screen builds an
/// in-memory list of items (via a fake "AI" description parser or manual entry) and saves each as
/// its own Firestore meal document.
@MainActor
final class AddMealViewModel: ObservableObject {
    @Published private(set) var aiDescription = ""
    @Published var isAnalyzing = false
    @Published var foodItems: [FoodItem] = []
    @Published var showAddManualForm = false

    @Published var manualName = ""
    @Published var manualCalories = ""
    @Published var manualProtein = ""
    @Published var manualCarbs = ""
    @Published var manualFat = ""
    @Published var manualMealType: MealType = .snacks
    @Published var manualError = ""

    @Published var isSaving = false
    @Published var saveError: String?

    let day: Int32
    private let store: NutritionStore

    init(store: NutritionStore, day: Int32) {
        self.store = store
        self.day = day
    }

    var canSave: Bool { !foodItems.isEmpty }

    var wordCount: Int { Self.wordCount(aiDescription) }

    private static func wordCount(_ text: String) -> Int {
        text.trimmingCharacters(in: .whitespaces).split(separator: " ").filter { !$0.isEmpty }.count
    }

    /// Mirrors Android's `onValueChange` gate on the description field exactly: accept the new
    /// value if it's within the 300-word cap, or if it's strictly shorter (always allow deleting
    /// even while over the cap).
    func updateDescription(_ newValue: String) {
        let newWordCount = Self.wordCount(newValue)
        if newWordCount <= 300 || newValue.count < aiDescription.count {
            aiDescription = newValue
        }
    }

    var totalCalories: Int32 { foodItems.reduce(0) { $0 + $1.calories } }
    var totalProtein: Float { foodItems.reduce(0) { $0 + $1.protein } }
    var totalCarbs: Float { foodItems.reduce(0) { $0 + $1.carbs } }
    var totalFat: Float { foodItems.reduce(0) { $0 + $1.fat } }

    func analyzeWithAI() {
        isAnalyzing = true
        foodItems.append(contentsOf: Self.parseMockItems(aiDescription))
        isAnalyzing = false
        aiDescription = ""
    }

    @discardableResult
    func addManualItem() -> Bool {
        let candidate = FoodItem(
            name: manualName.trimmingCharacters(in: .whitespaces),
            calories: Int32(manualCalories) ?? 0,
            protein: Float(manualProtein) ?? 0,
            carbs: Float(manualCarbs) ?? 0,
            fat: Float(manualFat) ?? 0,
            mealType: manualMealType
        )
        let errors = MealValidationKt.validateFoodItem(name: candidate.name, calories: candidate.calories, protein: candidate.protein, carbs: candidate.carbs, fat: candidate.fat)
        guard errors.isEmpty else {
            manualError = errors.first ?? ""
            return false
        }
        foodItems.append(candidate)
        manualName = ""; manualCalories = ""; manualProtein = ""; manualCarbs = ""; manualFat = ""
        manualMealType = .snacks
        manualError = ""
        showAddManualForm = false
        return true
    }

    func replaceItem(_ item: FoodItem) {
        guard let index = foodItems.firstIndex(where: { $0.id == item.id }) else { return }
        foodItems[index] = item
    }

    func removeItem(_ item: FoodItem) {
        foodItems.removeAll { $0.id == item.id }
    }

    func save(onSuccess: @escaping () -> Void) {
        // Validate all items before saving. Android leaves this as a dead branch (items are
        // already validated on add, so this never actually fires in practice) — kept for parity
        // rather than silently dropped.
        let allErrors = foodItems.compactMap { item -> String? in
            let errs = MealValidationKt.validateFoodItem(name: item.name, calories: item.calories, protein: item.protein, carbs: item.carbs, fat: item.fat)
            return errs.isEmpty ? nil : "\(item.name): \(errs.first ?? "")"
        }
        guard allErrors.isEmpty else { return }

        isSaving = true
        Task {
            do {
                // One Firestore meal document per item — matches Android's AppNavGraph.kt save
                // handler, which calls addMealForDay once per FoodItem, not once combined.
                for item in foodItems {
                    let meal = MealEntry(
                        id: UUID().uuidString,
                        name: item.name,
                        mealType: item.mealType.wireValue,
                        calories: item.calories,
                        protein: item.protein,
                        carbs: item.carbs,
                        fat: item.fat,
                        time: Self.currentTimeLabel()
                    )
                    try await store.addMeal(meal, to: day)
                }
                isSaving = false
                onSuccess()
            } catch {
                isSaving = false
                saveError = error.localizedDescription
            }
        }
    }

    private static func currentTimeLabel() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "h:mm a"
        return formatter.string(from: Date())
    }

    /// Fake/local "AI" — pure keyword matching, zero network call. Ported verbatim from
    /// Android's `AddMealScreen.kt#parseMockItems` so both platforms produce identical mock
    /// output for the same input. Must stay documented as mocked; never silently made "real" or
    /// dropped.
    static func parseMockItems(_ description: String) -> [FoodItem] {
        guard !description.trimmingCharacters(in: .whitespaces).isEmpty else { return [] }
        let normalized = description
            .replacingOccurrences(of: " and ", with: ",")
            .replacingOccurrences(of: "&", with: ",")
        let names = normalized.split(separator: ",")
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty }

        return names.map { name in
            let wc = name.split(separator: " ").count
            let lower = name.lowercased()
            let mealType: MealType
            if ["egg", "toast", "cereal", "oatmeal"].contains(where: { lower.contains($0) }) {
                mealType = .breakfast
            } else if ["rice", "roti", "dal", "curry", "salad"].contains(where: { lower.contains($0) }) {
                mealType = .lunch
            } else if ["soup", "steak", "pasta"].contains(where: { lower.contains($0) }) {
                mealType = .dinner
            } else {
                mealType = .snacks
            }
            let capitalized = name.prefix(1).uppercased() + name.dropFirst()
            return FoodItem(
                name: capitalized,
                calories: Int32(150 + wc * 50),
                protein: Float(8 + wc * 3),
                carbs: Float(20 + wc * 8),
                fat: Float(5 + wc * 2),
                mealType: mealType
            )
        }
    }
}
