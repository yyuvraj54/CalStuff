import SwiftUI
import CalStuffShared

/// Mirrors Android's `LogsScreen.kt` — full `CalendarProgressCard` (tap any day to select it),
/// month navigation, and the selected day's meals grouped by type below.
struct LogsView: View {
    @ObservedObject var store: NutritionStore
    @Environment(\.calStuffColors) private var colors
    @State private var selectedDay: Int32?

    private var effectiveSelectedDay: Int32 {
        if let selectedDay { return selectedDay }
        return store.isBrowsingCurrentMonth ? store.today.day : 1
    }

    private var selectedDayNutrition: DayNutrition? {
        store.browsedMonthLogs[effectiveSelectedDay]
    }

    private var highlightedDays: Set<Int32> {
        guard store.isBrowsingCurrentMonth else { return [] }
        return Set(1...store.today.day)
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    LogsCalendarCard(
                        percentage: store.browsedCalendarPercentage,
                        year: store.browsedYear,
                        month: store.browsedMonth,
                        highlightedDays: highlightedDays,
                        dayCalorieRatios: store.browsedDayCalorieRatios,
                        selectedDay: effectiveSelectedDay,
                        today: store.isBrowsingCurrentMonth ? store.today.day : nil,
                        onDayTap: { selectedDay = $0 }
                    )

                    MonthNavigationBar(
                        isCurrentMonth: store.isBrowsingCurrentMonth,
                        onPrevious: {
                            store.showPreviousMonth()
                            selectedDay = 1
                        },
                        onNext: {
                            store.showNextMonth()
                            selectedDay = 1
                        },
                        onJumpToCurrent: {
                            store.showCurrentMonth()
                            selectedDay = store.today.day
                        }
                    )

                    DaySummarySection(
                        day: selectedDayNutrition,
                        selectedDay: effectiveSelectedDay,
                        year: store.browsedYear,
                        month: store.browsedMonth
                    )
                }
                .padding(16)
                .padding(.bottom, 60)
            }
            .background(colors.background.ignoresSafeArea())
            .navigationTitle("Logs")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

private struct LogsCalendarCard: View {
    let percentage: Int
    let year: Int32
    let month: Int32
    let highlightedDays: Set<Int32>
    let dayCalorieRatios: [Int32: Float]
    let selectedDay: Int32?
    let today: Int32?
    let onDayTap: (Int32) -> Void
    @Environment(\.calStuffColors) private var colors

    private var monthName: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "LLLL"
        var components = DateComponents()
        components.year = Int(year)
        components.month = Int(month)
        components.day = 1
        let date = Calendar(identifier: .gregorian).date(from: components) ?? Date()
        return formatter.string(from: date)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Progress Statistics")
                    .font(.caption.weight(.medium))
                    .foregroundStyle(colors.onInverseSurface.opacity(0.6))
                Spacer()
                Text(monthName)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(colors.onInverseSurfaceVariant)
            }

            HStack(alignment: .bottom, spacing: 12) {
                Text("\(percentage)%")
                    .font(.system(size: 48, weight: .bold))
                    .foregroundStyle(colors.onInverseSurfaceVariant)
                Text("of the monthly plan\ncompleted")
                    .font(.caption)
                    .foregroundStyle(colors.onInverseSurface.opacity(0.5))
                    .padding(.bottom, 8)
            }

            MonthCalendarGrid(
                year: year, month: month,
                highlightedDays: highlightedDays,
                dayCalorieRatios: dayCalorieRatios,
                selectedDay: selectedDay,
                today: today,
                onDayTap: onDayTap
            )
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.inverseSurface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

private struct MonthNavigationBar: View {
    let isCurrentMonth: Bool
    let onPrevious: () -> Void
    let onNext: () -> Void
    let onJumpToCurrent: () -> Void
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        HStack {
            navButton("chevron.left", action: onPrevious)
            Spacer()
            if !isCurrentMonth {
                Button(action: onJumpToCurrent) {
                    HStack(spacing: 6) {
                        Image(systemName: "calendar")
                        Text("Back to current month").font(.caption.weight(.semibold))
                    }
                    .foregroundStyle(colors.onInverseSurface)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(colors.inverseSurface)
                    .clipShape(Capsule())
                }
            }
            Spacer()
            navButton("chevron.right", action: onNext)
        }
    }

    private func navButton(_ systemImage: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .foregroundStyle(colors.textPrimary)
                .frame(width: 40, height: 40)
                .background(colors.surfaceVariant)
                .clipShape(Circle())
        }
    }
}

private struct DaySummarySection: View {
    let day: DayNutrition?
    let selectedDay: Int32
    let year: Int32
    let month: Int32
    @Environment(\.calStuffColors) private var colors

    private var monthName: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "LLLL"
        var components = DateComponents()
        components.year = Int(year)
        components.month = Int(month)
        components.day = 1
        let date = Calendar(identifier: .gregorian).date(from: components) ?? Date()
        return formatter.string(from: date)
    }

    private var groupedMeals: [(type: String, meals: [MealEntry])] {
        let meals = day?.meals ?? []
        let order = ["BREAKFAST", "LUNCH", "DINNER", "SNACKS"]
        return order.compactMap { type in
            let matching = meals.filter { $0.mealType == type }
            return matching.isEmpty ? nil : (type, matching)
        }
    }

    private var totalCalories: Int {
        (day?.meals ?? []).reduce(0) { $0 + Int($1.calories) }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(alignment: .lastTextBaseline) {
                Text("\(monthName) \(selectedDay), \(year)")
                    .font(.title3.bold())
                    .foregroundStyle(colors.textPrimary)
                Spacer()
                if day != nil {
                    HStack(spacing: 4) {
                        Text("\(totalCalories)").fontWeight(.bold).foregroundStyle(colors.textPrimary)
                        Text("/ \(day?.calorieGoal ?? 2000) kcal").foregroundStyle(colors.textSecondary)
                    }
                    .font(.subheadline)
                }
            }

            if groupedMeals.isEmpty {
                VStack(spacing: 8) {
                    Image(systemName: "tray").font(.largeTitle).foregroundStyle(colors.textSecondary)
                    Text("No meals logged").font(.headline).foregroundStyle(colors.textPrimary)
                    Text("Nothing recorded for this day").font(.caption).foregroundStyle(colors.textSecondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 32)
            } else {
                ForEach(groupedMeals, id: \.type) { group in
                    ForEach(group.meals, id: \.id) { meal in
                        LogMealCard(meal: meal)
                    }
                }
            }
        }
    }
}

private struct LogMealCard: View {
    let meal: MealEntry
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                HStack(spacing: 8) {
                    Circle().fill(MealTypeStyle.color(forWireValue: meal.mealType, colors)).frame(width: 10, height: 10)
                    Text(MealTypeStyle.label(forWireValue: meal.mealType)).font(.caption.weight(.medium)).foregroundStyle(colors.textSecondary)
                }
                Spacer()
                Text(meal.time).font(.caption).foregroundStyle(colors.textSecondary)
            }
            Text(meal.name).font(.headline).foregroundStyle(colors.textPrimary)
            HStack {
                Text("\(meal.calories) kcal").font(.subheadline.weight(.bold)).foregroundStyle(colors.textPrimary)
                Spacer()
                HStack(spacing: 8) {
                    macroPill("P", "\(Int(meal.protein))g", colors.highlight)
                    macroPill("C", "\(Int(meal.carbs))g", colors.error)
                    macroPill("F", "\(Int(meal.fat))g", colors.textPrimary)
                }
            }
        }
        .padding(16)
        .background(colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
    }

    private func macroPill(_ label: String, _ value: String, _ color: Color) -> some View {
        HStack(spacing: 2) {
            Text(label).font(.caption2.weight(.bold)).foregroundStyle(color)
            Text(value).font(.caption2.weight(.medium)).foregroundStyle(colors.textPrimary.opacity(0.7))
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(color.opacity(0.12))
        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
    }
}
