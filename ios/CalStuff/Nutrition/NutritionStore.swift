import Foundation
import CalStuffShared

/// Shared observable state for the Home/Meals/Logs/AddMeal tab group — one Firestore
/// subscription per data source (current month's logs, streak) instead of each screen
/// re-subscribing independently. Created once in `MainTabView`, passed down to each tab.
@MainActor
final class NutritionStore: ObservableObject {
    @Published private(set) var monthLogs: [Int32: DayNutrition] = [:]
    @Published private(set) var streak = StreakData(currentStreak: 0, longestStreak: 0, lastLoggedDate: "")
    @Published private(set) var profile: UserProfile?
    @Published private(set) var displayName: String = ""

    /// Independent from `monthLogs` (which always tracks *today's* month for Home/Meals) — Logs
    /// lets the user page through other months without disturbing Home's data.
    @Published private(set) var browsedYear: Int32
    @Published private(set) var browsedMonth: Int32
    @Published private(set) var browsedMonthLogs: [Int32: DayNutrition] = [:]

    let dependencies: AppDependencies
    private var monthSubscription: FlowSubscription?
    private var browsedMonthSubscription: FlowSubscription?
    private var streakSubscription: FlowSubscription?

    struct DateParts: Equatable {
        let year: Int32
        let month: Int32
        let day: Int32
    }

    let today: DateParts

    init(dependencies: AppDependencies) {
        self.dependencies = dependencies
        let c = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        let year = Int32(c.year ?? 2026), month = Int32(c.month ?? 1)
        self.today = DateParts(year: year, month: month, day: Int32(c.day ?? 1))
        self.browsedYear = year
        self.browsedMonth = month
        self.displayName = dependencies.displayNameStore.get() ?? ""
        observeMonth(year: today.year, month: today.month)
        observeBrowsedMonth()
        observeStreak()
        Task { await loadProfile() }
    }

    private func observeMonth(year: Int32, month: Int32) {
        monthSubscription?.cancel()
        let flow = dependencies.nutritionRepository.observeMonthLogs(year: year, month: month)
        monthSubscription = FlowBridgeKt.watch(flow) { [weak self] value in
            guard let dict = value as? [Int32: DayNutrition] else { return }
            self?.monthLogs = dict
        }
    }

    private func observeBrowsedMonth() {
        browsedMonthSubscription?.cancel()
        // The Firestore repo caches one Flow per (year, month), so when browsedMonth == today's
        // month this reuses the same underlying listener `observeMonth` already created above —
        // no duplicate Firestore subscription.
        let flow = dependencies.nutritionRepository.observeMonthLogs(year: browsedYear, month: browsedMonth)
        browsedMonthSubscription = FlowBridgeKt.watch(flow) { [weak self] value in
            guard let dict = value as? [Int32: DayNutrition] else { return }
            self?.browsedMonthLogs = dict
        }
    }

    var isBrowsingCurrentMonth: Bool {
        browsedYear == today.year && browsedMonth == today.month
    }

    func showPreviousMonth() {
        if browsedMonth == 1 {
            browsedMonth = 12
            browsedYear -= 1
        } else {
            browsedMonth -= 1
        }
        observeBrowsedMonth()
    }

    func showNextMonth() {
        if browsedMonth == 12 {
            browsedMonth = 1
            browsedYear += 1
        } else {
            browsedMonth += 1
        }
        observeBrowsedMonth()
    }

    func showCurrentMonth() {
        browsedYear = today.year
        browsedMonth = today.month
        observeBrowsedMonth()
    }

    /// Day-of-month → consumed/goal ratio for the *browsed* month (full month, not just this
    /// week) — used by the Logs calendar grid.
    var browsedDayCalorieRatios: [Int32: Float] {
        guard calorieGoal > 0 else { return [:] }
        var ratios: [Int32: Float] = [:]
        for (day, nutrition) in browsedMonthLogs {
            let calories = nutrition.meals.reduce(0) { $0 + Int($1.calories) }
            if calories > 0 {
                ratios[day] = Float(calories) / Float(calorieGoal)
            }
        }
        return ratios
    }

    var browsedCalendarPercentage: Int {
        guard isBrowsingCurrentMonth else { return 0 }
        return calendarPercentage
    }

    private func observeStreak() {
        streakSubscription = FlowBridgeKt.watch(dependencies.streakRepository.observeStreak()) { [weak self] value in
            guard let streak = value as? StreakData else { return }
            self?.streak = streak
        }
    }

    private func loadProfile() async {
        profile = try? await dependencies.userProfileRepository.getProfile()
        if let name = profile?.name, !name.isEmpty {
            displayName = name
        }
    }

    var todayNutrition: DayNutrition? {
        monthLogs[today.day]
    }

    var calorieGoal: Int32 {
        profile?.dailyCalorieGoal ?? 2000
    }

    /// Full Mon–Sun week with today inserted at the correct index and future days empty —
    /// mirrors Android's `HomeScreenData.weekDays` exactly (same Monday=0 indexing).
    var weekDays: [DayProgress] {
        let calendar = Calendar(identifier: .gregorian)
        let weekday = calendar.component(.weekday, from: Date()) // Sun=1 ... Sat=7
        let todayIndex = (weekday + 5) % 7 // Monday=0 .. Sunday=6
        let dayNames = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]

        return (0...6).map { i -> DayProgress in
            let dayNumber = today.day + Int32(i - todayIndex)
            let calories = dayNumber == today.day
                ? Int((todayNutrition?.meals ?? []).reduce(0) { $0 + Int($1.calories) })
                : (dayNumber > 0 && dayNumber < today.day ? (monthLogs[dayNumber]?.meals.reduce(0) { $0 + Int($1.calories) } ?? 0) : 0)
            return DayProgress(dayName: dayNames[i], date: "\(dayNumber)", caloriesConsumed: calories)
        }
    }

    /// Day-of-month → consumed/goal ratio, this week only (data-bearing days only) — for the
    /// calendar widget's progress rings.
    var dayCalorieRatios: [Int32: Float] {
        guard calorieGoal > 0 else { return [:] }
        var ratios: [Int32: Float] = [:]
        for entry in weekDays {
            guard let dayNumber = Int32(entry.date), entry.caloriesConsumed > 0 else { continue }
            ratios[dayNumber] = Float(entry.caloriesConsumed) / Float(calorieGoal)
        }
        return ratios
    }

    var calendarPercentage: Int {
        let calendar = Calendar.current
        guard let range = calendar.range(of: .day, in: .month, for: Date()) else { return 0 }
        return (Int(today.day) * 100) / range.count
    }

    var bmiConfig: BmiConfig? {
        guard let profile, profile.heightCm > 0, profile.weightKg > 0 else { return nil }
        let gender: BmiGender = profile.gender == Gender.male.wireValue ? .male : (profile.gender == Gender.female.wireValue ? .female : .other)
        return BmiConfig(heightCm: profile.heightCm, weightKg: profile.weightKg, age: Int(profile.age), gender: gender)
    }

    func addMeal(_ meal: MealEntry, to day: Int32, year: Int32? = nil, month: Int32? = nil) async throws {
        let y = year ?? today.year, m = month ?? today.month
        _ = try await dependencies.nutritionRepository.addMeal(year: y, month: m, day: day, meal: meal)
        if y == today.year, m == today.month, day == today.day {
            _ = try? await dependencies.streakRepository.updateStreak(todayDate: DateKey.shared.of(year: today.year, month: today.month, day: today.day))
        }
    }

    func deleteMeal(_ mealId: String, from day: Int32, year: Int32? = nil, month: Int32? = nil) async throws {
        _ = try await dependencies.nutritionRepository.deleteMeal(year: year ?? today.year, month: month ?? today.month, day: day, mealId: mealId)
    }

    func updateMeal(_ meal: MealEntry, in day: Int32, year: Int32? = nil, month: Int32? = nil) async throws {
        _ = try await dependencies.nutritionRepository.updateMeal(year: year ?? today.year, month: month ?? today.month, day: day, meal: meal)
    }
}
