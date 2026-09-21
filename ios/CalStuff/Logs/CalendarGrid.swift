import SwiftUI

/// Mirrors Android's `DayCircle.kt` — a day-of-month cell with a calorie-ratio progress ring
/// (green→red as it fills, full red once exceeded), today/selected states.
struct DayCircle: View {
    let day: Int32
    let isHighlighted: Bool
    var isToday: Bool = false
    var isSelected: Bool = false
    var calorieRatio: Float?
    let onTap: (() -> Void)?
    @Environment(\.calStuffColors) private var colors

    private var hasCalorieData: Bool { (calorieRatio ?? 0) > 0 }

    private var backgroundColor: Color {
        if isSelected { return colors.onInverseSurface }
        if isHighlighted { return colors.onInverseSurfaceVariant }
        return colors.inverseSurface
    }

    private var textColor: Color {
        if isSelected { return colors.inverseSurface }
        if isToday { return colors.error }
        if isHighlighted { return colors.inverseSurface }
        return colors.onInverseSurfaceVariant
    }

    var body: some View {
        GeometryReader { geo in
            let size = geo.size.width
            ZStack {
                if hasCalorieData, let ratio = calorieRatio {
                    let clamped = min(max(ratio, 0), 1.5)
                    let exceeded = ratio > 1
                    let ringColor = exceeded ? colors.error : colors.success.mix(with: colors.error, amount: Double(clamped))
                    Circle().stroke(ringColor.opacity(0.15), lineWidth: max(size * 0.08, 2))
                    Circle()
                        .trim(from: 0, to: CGFloat(min(clamped, 1)))
                        .stroke(ringColor, style: StrokeStyle(lineWidth: max(size * 0.08, 2), lineCap: .round))
                        .rotationEffect(.degrees(-90))
                } else if isToday {
                    Circle().stroke(colors.error, lineWidth: 2)
                }

                Circle()
                    .fill(backgroundColor)
                    .padding(hasCalorieData ? min(max(size * 0.1, 2), 4) : 0)
                    .overlay(
                        Text("\(day)")
                            .font(.system(size: size * 0.38, weight: .bold))
                            .foregroundStyle(textColor)
                    )
            }
            .contentShape(Circle())
            .onTapGesture { onTap?() }
        }
        .aspectRatio(1, contentMode: .fit)
    }
}

/// Mirrors Android's `MonthCalendarGrid.kt` — fixed 6-row/7-column grid so height stays constant
/// across months, Monday-first day headers.
struct MonthCalendarGrid: View {
    let year: Int32
    let month: Int32
    let highlightedDays: Set<Int32>
    let dayCalorieRatios: [Int32: Float]
    let selectedDay: Int32?
    let today: Int32?
    let onDayTap: (Int32) -> Void

    private let dayHeaders = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
    @Environment(\.calStuffColors) private var colors

    private var firstWeekdayOffset: Int {
        var components = DateComponents()
        components.year = Int(year)
        components.month = Int(month)
        components.day = 1
        let calendar = Calendar(identifier: .gregorian)
        guard let date = calendar.date(from: components) else { return 0 }
        let weekday = calendar.component(.weekday, from: date) // Sun=1...Sat=7
        return (weekday + 5) % 7 // Monday=0..Sunday=6
    }

    private var daysInMonth: Int {
        var components = DateComponents()
        components.year = Int(year)
        components.month = Int(month)
        let calendar = Calendar(identifier: .gregorian)
        guard let date = calendar.date(from: components), let range = calendar.range(of: .day, in: .month, for: date) else { return 30 }
        return range.count
    }

    /// 42 cells (6×7); `nil` = blank leading/trailing cell.
    private var cells: [Int32?] {
        var result: [Int32?] = Array(repeating: nil, count: firstWeekdayOffset)
        result.append(contentsOf: (1...daysInMonth).map { Int32($0) })
        while result.count < 42 { result.append(nil) }
        return result
    }

    var body: some View {
        VStack(spacing: 6) {
            HStack(spacing: 6) {
                ForEach(dayHeaders, id: \.self) { name in
                    Text(name)
                        .font(.caption2.weight(.medium))
                        .foregroundStyle(colors.onInverseSurface.opacity(0.4))
                        .frame(maxWidth: .infinity)
                }
            }
            ForEach(0..<6, id: \.self) { row in
                HStack(spacing: 6) {
                    ForEach(0..<7, id: \.self) { col in
                        let day = cells[row * 7 + col]
                        if let day {
                            DayCircle(
                                day: day,
                                isHighlighted: highlightedDays.contains(day),
                                isToday: today == day,
                                isSelected: selectedDay == day,
                                calorieRatio: dayCalorieRatios[day],
                                onTap: { onDayTap(day) }
                            )
                            .frame(maxWidth: .infinity)
                        } else {
                            Color.clear.frame(maxWidth: .infinity, minHeight: 1)
                        }
                    }
                }
            }
        }
    }
}
