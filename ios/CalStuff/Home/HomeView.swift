import SwiftUI
import CalStuffShared

/// Mirrors Android's `HomeScreen.kt` widget layout exactly: header, a StreakCard + Calendar-
/// ProgressCardCompact row, then DailyProgressCard, WeeklyLineChartCard, and BmiCard full-width —
/// a fixed vertical stack (Android's drag-and-drop `WidgetGrid` exists in the codebase but is
/// dead code, never wired into the real Home screen, so it isn't ported here).
struct HomeView: View {
    @ObservedObject var store: NutritionStore
    @ObservedObject var rootViewModel: RootViewModel
    let onLoggedOut: () -> Void
    @Environment(\.calStuffColors) private var colors
    @State private var showingAddMeal = false

    private var monthAbbreviation: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM"
        return formatter.string(from: Date())
    }

    private var consumedThisWeek: Int {
        store.weekDays.reduce(0) { $0 + $1.caloriesConsumed }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    HStack(alignment: .top) {
                        greeting
                        Spacer()
                        NavigationLink {
                            ProfileView(dependencies: store.dependencies, rootViewModel: rootViewModel, onLoggedOut: onLoggedOut)
                        } label: {
                            ZStack {
                                Circle().fill(colors.inverseSurface).frame(width: 44, height: 44)
                                Image(systemName: "person.fill").foregroundStyle(colors.onInverseSurface)
                            }
                        }
                    }

                    HStack(alignment: .top, spacing: 16) {
                        StreakCard(streakDays: Int(store.streak.currentStreak))
                        CalendarProgressCardCompact(
                            percentage: store.calendarPercentage,
                            monthAbbreviation: monthAbbreviation,
                            weekDays: store.weekDays,
                            todayDate: store.today.day,
                            dayCalorieRatios: store.dayCalorieRatios,
                            consumedThisWeek: consumedThisWeek,
                            calorieGoal: store.calorieGoal
                        )
                    }

                    DailyProgressCard(
                        calories: todayCalories,
                        goal: store.calorieGoal,
                        protein: todayMacro(\.protein),
                        carbs: todayMacro(\.carbs),
                        fat: todayMacro(\.fat)
                    )

                    WeeklyLineChartCard(days: store.weekDays, goal: store.calorieGoal)

                    if let bmiConfig = store.bmiConfig {
                        BmiCard(config: bmiConfig)
                    }
                }
                .padding(20)
                .padding(.bottom, 60)
            }
            .background(colors.background.ignoresSafeArea())
            .overlay(alignment: .bottomTrailing) {
                Button(action: { showingAddMeal = true }) {
                    Image(systemName: "plus")
                        .font(.title2.weight(.semibold))
                        .foregroundStyle(colors.onHighlight)
                        .frame(width: 56, height: 56)
                        .background(colors.highlight)
                        .clipShape(Circle())
                        .shadow(radius: 8, y: 4)
                }
                .padding(24)
            }
            .sheet(isPresented: $showingAddMeal) {
                AddMealView(store: store, day: store.today.day)
            }
        }
    }

    private var todayCalories: Int {
        (store.todayNutrition?.meals ?? []).reduce(0) { $0 + Int($1.calories) }
    }

    private func todayMacro(_ keyPath: KeyPath<MealEntry, Float>) -> Float {
        (store.todayNutrition?.meals ?? []).reduce(0) { $0 + $1[keyPath: keyPath] }
    }

    private var greeting: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Hi, \(store.displayName.isEmpty ? "there" : store.displayName)")
                .font(.title.bold())
                .foregroundStyle(colors.textPrimary)
            Text("Let's check today's progress")
                .font(.subheadline)
                .foregroundStyle(colors.textSecondary)
        }
    }
}
