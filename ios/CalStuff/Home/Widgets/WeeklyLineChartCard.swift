import SwiftUI

/// Mirrors Android's `WeeklyLineChartCard.kt` — a real line chart (not bars): 7 day-points
/// connected by height-colored segments (yellow near goal, redder as it climbs toward/over goal),
/// a dashed horizontal goal line, and day-name/date labels under each point.
struct WeeklyLineChartCard: View {
    let days: [DayProgress]
    let goal: Int32
    @Environment(\.calStuffColors) private var colors

    private var average: Int {
        let withData = days.filter { $0.caloriesConsumed > 0 }
        guard !withData.isEmpty else { return 0 }
        return withData.reduce(0) { $0 + $1.caloriesConsumed } / withData.count
    }
    private var isEmpty: Bool { days.allSatisfy { $0.caloriesConsumed == 0 } }
    private var maxValue: Int {
        max(Int(goal), days.map(\.caloriesConsumed).max() ?? 0, 1)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            header
            if isEmpty {
                emptyState
            } else {
                chart
                xAxisLabels
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.surfaceVariant)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }

    private var header: some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading, spacing: 2) {
                Text("Weekly Trend").font(.headline).foregroundStyle(colors.textPrimary)
                Text("Avg \(average) kcal/day").font(.caption).foregroundStyle(colors.textSecondary)
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 2) {
                Text("Daily Goal").font(.caption2).foregroundStyle(colors.textSecondary)
                Text("\(goal) kcal").font(.caption.weight(.semibold)).foregroundStyle(colors.textPrimary)
            }
        }
    }

    private var emptyState: some View {
        VStack(spacing: 8) {
            Image(systemName: "chart.line.uptrend.xyaxis")
                .font(.largeTitle)
                .foregroundStyle(colors.textSecondary)
            Text("No data yet").font(.subheadline).foregroundStyle(colors.textSecondary)
        }
        .frame(maxWidth: .infinity, minHeight: 120)
    }

    private var chart: some View {
        Canvas { context, size in
            let goalY = size.height * (1 - CGFloat(goal) / CGFloat(maxValue))
            var goalPath = Path()
            goalPath.move(to: CGPoint(x: 0, y: goalY))
            goalPath.addLine(to: CGPoint(x: size.width, y: goalY))
            context.stroke(goalPath, with: .color(colors.textSecondary.opacity(0.4)), style: StrokeStyle(lineWidth: 1, dash: [4, 4]))

            let stepX = size.width / CGFloat(max(days.count - 1, 1))
            let points: [CGPoint] = days.enumerated().map { i, day in
                CGPoint(x: CGFloat(i) * stepX, y: size.height * (1 - CGFloat(day.caloriesConsumed) / CGFloat(maxValue)))
            }

            for i in 0..<max(points.count - 1, 0) {
                var segment = Path()
                segment.move(to: points[i])
                segment.addLine(to: points[i + 1])
                let avgY = (points[i].y + points[i + 1].y) / 2
                let heightRatio = 1 - avgY / size.height
                let segmentColor = colors.highlight.mix(with: colors.error, amount: Double(heightRatio))
                context.stroke(segment, with: .color(segmentColor), style: StrokeStyle(lineWidth: 3, lineCap: .round))
            }

            for (i, point) in points.enumerated() {
                guard days[i].caloriesConsumed > 0 else { continue }
                let dotColor = colors.highlight.mix(with: colors.error, amount: Double(1 - point.y / size.height))
                context.fill(Path(ellipseIn: CGRect(x: point.x - 5, y: point.y - 5, width: 10, height: 10)), with: .color(dotColor))
                context.fill(Path(ellipseIn: CGRect(x: point.x - 2.5, y: point.y - 2.5, width: 5, height: 5)), with: .color(colors.surfaceVariant))
            }
        }
        .frame(height: 120)
    }

    private var xAxisLabels: some View {
        HStack {
            ForEach(days, id: \.date) { day in
                VStack(spacing: 0) {
                    Text(day.dayName).font(.caption2).foregroundStyle(colors.textSecondary)
                    Text(day.date).font(.caption2.weight(.medium)).foregroundStyle(colors.textPrimary)
                }
                .frame(maxWidth: .infinity)
            }
        }
    }
}
