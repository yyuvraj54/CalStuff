import SwiftUI

/// Mirrors Android's `CalendarProgressCardCompact.kt` — the deliberately-inverted "dark card"
/// style (`colors.inverseSurface`), showing this week's day circles with per-day calorie-ratio
/// progress rings.
struct CalendarProgressCardCompact: View {
    let percentage: Int
    let monthAbbreviation: String
    let weekDays: [DayProgress]
    let todayDate: Int32
    let dayCalorieRatios: [Int32: Float]
    let consumedThisWeek: Int
    let calorieGoal: Int32
    @Environment(\.calStuffColors) private var colors

    private let letters = ["M", "T", "W", "T", "F", "S", "S"]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top) {
                Spacer()
                VStack(alignment: .trailing, spacing: 0) {
                    Text("\(percentage)%")
                        .font(.title.bold())
                        .foregroundStyle(colors.onInverseSurface)
                    Text(monthAbbreviation)
                        .font(.caption)
                        .foregroundStyle(colors.onInverseSurface.opacity(0.6))
                }
            }

            HStack(spacing: 4) {
                ForEach(Array(letters.enumerated()), id: \.offset) { _, letter in
                    Text(letter)
                        .font(.caption2.weight(.medium))
                        .foregroundStyle(colors.onInverseSurface.opacity(0.5))
                        .frame(maxWidth: .infinity)
                }
            }

            HStack(spacing: 4) {
                ForEach(weekDays, id: \.date) { entry in
                    let dayNumber = Int32(entry.date) ?? 0
                    DayCircle(
                        day: dayNumber,
                        isHighlighted: dayNumber > 0 && dayNumber <= todayDate,
                        isToday: dayNumber == todayDate,
                        calorieRatio: dayCalorieRatios[dayNumber],
                        onTap: nil
                    )
                    .frame(maxWidth: .infinity)
                }
            }

            Text(calorieGoal > 0 ? "\(consumedThisWeek) / \(calorieGoal) kcal" : "\(percentage)% of \(monthAbbreviation)")
                .font(.caption2)
                .foregroundStyle(colors.onInverseSurface.opacity(0.4))
        }
        .padding(16)
        .frame(maxWidth: .infinity, minHeight: 150, alignment: .topLeading)
        .background(colors.inverseSurface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}
