import Foundation
import FirebaseAuth
import FirebaseFirestore
import CalStuffShared

/// Firestore-backed `NutritionRepository` — same documents Android writes
/// (`users/{uid}/nutrition/{YYYY-MM-DD}`, `date` excluded from the body since it's the doc ID,
/// `calorieGoal: Int`, `meals: [MealEntryDto-shaped map]`).
///
/// Android hand-rolls a Room cache + `pendingSync` bookkeeping around this because it never uses
/// Firestore realtime listeners. This uses a real `addSnapshotListener` for reads and
/// `runTransaction` for read-modify-write mutations instead — simpler, and still correct under
/// concurrent writes (which the Room approach only handled via a manual "pending" flag).
final class NutritionFirestoreRepository: NutritionRepository {

    private let firestore = Firestore.firestore()
    private var monthListeners: [String: ListenerRegistration] = [:]
    private var monthFlows: [String: any MutableStateFlow] = [:]

    private func currentUserId() -> String? {
        Auth.auth().currentUser?.uid
    }

    func observeMonthLogs(year: Int32, month: Int32) -> any Flow {
        let key = "\(year)-\(month)"
        if let existing = monthFlows[key] {
            return existing
        }
        let flow = StateFlowKt.MutableStateFlow(value: [Int32: DayNutrition]())
        monthFlows[key] = flow

        guard let uid = currentUserId() else {
            return flow
        }
        let start = Self.dateKey(year, month, 1)
        let end = Self.dateKey(year, month, 31)
        monthListeners[key]?.remove()
        monthListeners[key] = firestore.collection("users").document(uid).collection("nutrition")
            .whereField(FieldPath.documentID(), isGreaterThanOrEqualTo: start)
            .whereField(FieldPath.documentID(), isLessThanOrEqualTo: end)
            .addSnapshotListener { snapshot, error in
                if let error {
                    AppLog.error("NutritionFirestoreRepository.observeMonthLogs", error)
                    return
                }
                var result: [Int32: DayNutrition] = [:]
                for doc in snapshot?.documents ?? [] {
                    guard let (y, m, d) = Self.parseDateKey(doc.documentID), y == year, m == month else { continue }
                    result[d] = Self.dayNutrition(from: doc.data(), year: y, month: m, day: d)
                }
                flow.setValue(result)
            }
        return flow
    }

    func addMeal(year: Int32, month: Int32, day: Int32, meal: MealEntry) async throws -> Any? {
        try await mutateDay(year: year, month: month, day: day) { data in
            var data = data
            var meals = (data["meals"] as? [[String: Any]]) ?? []
            meals.append(Self.dict(from: meal))
            data["meals"] = meals
            return data
        }
        return nil
    }

    func updateMeal(year: Int32, month: Int32, day: Int32, meal: MealEntry) async throws -> Any? {
        try await mutateDay(year: year, month: month, day: day) { data in
            var data = data
            var meals = (data["meals"] as? [[String: Any]]) ?? []
            if let idx = meals.firstIndex(where: { ($0["id"] as? String) == meal.id }) {
                meals[idx] = Self.dict(from: meal)
            } else {
                meals.append(Self.dict(from: meal))
            }
            data["meals"] = meals
            return data
        }
        return nil
    }

    func deleteMeal(year: Int32, month: Int32, day: Int32, mealId: String) async throws -> Any? {
        try await mutateDay(year: year, month: month, day: day) { data in
            var data = data
            var meals = (data["meals"] as? [[String: Any]]) ?? []
            meals.removeAll { ($0["id"] as? String) == mealId }
            data["meals"] = meals
            return data
        }
        return nil
    }

    func setDayCalorieGoal(year: Int32, month: Int32, day: Int32, goal: Int32) async throws -> Any? {
        try await mutateDay(year: year, month: month, day: day) { data in
            var data = data
            data["calorieGoal"] = goal
            return data
        }
        return nil
    }

    // MARK: - Transaction helper

    private func mutateDay(year: Int32, month: Int32, day: Int32, _ mutate: @escaping ([String: Any]) -> [String: Any]) async throws {
        guard let uid = currentUserId() else {
            throw NSError(domain: "CalStuff", code: 10, userInfo: [NSLocalizedDescriptionKey: "Not authenticated"])
        }
        let ref = firestore.collection("users").document(uid).collection("nutrition").document(Self.dateKey(year, month, day))
        _ = try await firestore.runTransaction { transaction, errorPointer in
            let snapshot: DocumentSnapshot
            do {
                snapshot = try transaction.getDocument(ref)
            } catch {
                errorPointer?.pointee = error as NSError
                return nil
            }
            let existing = snapshot.data() ?? ["calorieGoal": 2000, "meals": [[String: Any]]()]
            transaction.setData(mutate(existing), forDocument: ref)
            return nil
        }
    }

    deinit {
        monthListeners.values.forEach { $0.remove() }
    }

    // MARK: - Mapping (mirrors Android's DayLogDto/MealEntryDto)

    private static func dateKey(_ year: Int32, _ month: Int32, _ day: Int32) -> String {
        String(format: "%04d-%02d-%02d", year, month, day)
    }

    private static func parseDateKey(_ key: String) -> (Int32, Int32, Int32)? {
        let parts = key.split(separator: "-")
        guard parts.count == 3,
              let y = Int32(parts[0]), let m = Int32(parts[1]), let d = Int32(parts[2]) else { return nil }
        return (y, m, d)
    }

    private static func dayNutrition(from data: [String: Any], year: Int32, month: Int32, day: Int32) -> DayNutrition {
        let mealDicts = (data["meals"] as? [[String: Any]]) ?? []
        return DayNutrition(
            year: year, month: month, day: day,
            calorieGoal: int32(data, "calorieGoal", default: 2000),
            meals: mealDicts.map { meal(from: $0) }
        )
    }

    private static func dict(from meal: MealEntry) -> [String: Any] {
        [
            "id": meal.id,
            "name": meal.name,
            "mealType": meal.mealType,
            "calories": meal.calories,
            "protein": meal.protein,
            "carbs": meal.carbs,
            "fat": meal.fat,
            "time": meal.time,
        ]
    }

    private static func meal(from data: [String: Any]) -> MealEntry {
        MealEntry(
            id: string(data, "id"),
            name: string(data, "name"),
            mealType: string(data, "mealType"),
            calories: int32(data, "calories"),
            protein: float(data, "protein"),
            carbs: float(data, "carbs"),
            fat: float(data, "fat"),
            time: string(data, "time")
        )
    }

    private static func string(_ data: [String: Any], _ key: String) -> String {
        data[key] as? String ?? ""
    }

    private static func int32(_ data: [String: Any], _ key: String, default def: Int32 = 0) -> Int32 {
        (data[key] as? NSNumber)?.int32Value ?? def
    }

    private static func float(_ data: [String: Any], _ key: String) -> Float {
        (data[key] as? NSNumber)?.floatValue ?? 0
    }
}
