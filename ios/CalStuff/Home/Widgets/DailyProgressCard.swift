import SwiftUI

/// Mirrors Android's `DailyProgressCard.kt` + `NutritionRing.kt` — a segmented ring (protein/
/// carbs/fat, gapped, rounded caps, floating % bubbles) rather than a plain circular progress.
struct DailyProgressCard: View {
    let calories: Int
    let goal: Int32
    let protein: Float
    let carbs: Float
    let fat: Float
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(alignment: .top) {
                Text("Calories").font(.headline).foregroundStyle(colors.textPrimary)
                Spacer()
                VStack(alignment: .trailing, spacing: 0) {
                    Text("Daily Goal").font(.caption2).foregroundStyle(colors.textSecondary)
                    HStack(spacing: 4) {
                        Text("\(goal)").fontWeight(.bold).foregroundStyle(colors.textPrimary)
                        Text("kcal").font(.caption).foregroundStyle(colors.textSecondary)
                    }
                }
            }
            HStack {
                Spacer()
                NutritionRing(protein: protein, carbs: carbs, fat: fat, calories: calories)
                Spacer()
            }
            HStack(spacing: 12) {
                NutrientIndicator(color: colors.highlight, value: protein, label: "Protein")
                NutrientIndicator(color: colors.error, value: carbs, label: "Carbs")
                NutrientIndicator(color: colors.inverseSurface, value: fat, label: "Fat")
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

struct NutritionRing: View {
    let protein: Float
    let carbs: Float
    let fat: Float
    let calories: Int
    @Environment(\.calStuffColors) private var colors

    private var total: Float { protein + carbs + fat }
    private let gapDegrees: Double = 8
    private let minSweepDegrees: Double = 12
    private let ringSize: CGFloat = 190
    private let lineWidth: CGFloat = 24

    private struct Segment {
        let color: Color
        let startDegrees: Double
        let sweepDegrees: Double
        let percentText: String
    }

    private var segments: [Segment] {
        guard total > 0 else { return [] }
        let values: [Float] = [protein, carbs, fat]
        let available = 360.0 - gapDegrees * 3
        let raw = values.map { max(minSweepDegrees, Double($0 / total) * available) }
        let scale = available / raw.reduce(0, +)
        let sweeps = raw.map { $0 * scale }
        let ringColors = [colors.highlight, colors.error, colors.inverseSurface]

        var start = -90.0
        return (0..<3).map { i in
            let pct = total > 0 ? Int((values[i] / total * 100).rounded()) : 0
            let segment = Segment(color: ringColors[i], startDegrees: start, sweepDegrees: sweeps[i], percentText: "\(pct)%")
            start += sweeps[i] + gapDegrees
            return segment
        }
    }

    var body: some View {
        ZStack {
            if segments.isEmpty {
                Circle().stroke(colors.divider, lineWidth: lineWidth)
            } else {
                ForEach(Array(segments.enumerated()), id: \.offset) { _, segment in
                    Circle()
                        .trim(from: 0, to: CGFloat(segment.sweepDegrees / 360))
                        .stroke(segment.color, style: StrokeStyle(lineWidth: lineWidth, lineCap: .round))
                        .rotationEffect(.degrees(segment.startDegrees))
                }
                ForEach(Array(segments.enumerated()), id: \.offset) { _, segment in
                    let midAngle = (segment.startDegrees + segment.sweepDegrees / 2) * .pi / 180
                    let radius = ringSize / 2
                    Text(segment.percentText)
                        .font(.system(size: 10, weight: .bold))
                        .foregroundStyle(.white)
                        .padding(6)
                        .background(Circle().fill(segment.color))
                        .offset(x: radius * cos(midAngle), y: radius * sin(midAngle))
                }
            }
            VStack(spacing: 0) {
                Text("\(calories)").font(.title.bold()).foregroundStyle(colors.textPrimary)
                Text("kcal").font(.caption2).foregroundStyle(colors.textSecondary)
            }
        }
        .frame(width: ringSize, height: ringSize)
        .animation(.spring(response: 0.6, dampingFraction: 0.8), value: calories)
    }
}

struct NutrientIndicator: View {
    let color: Color
    let value: Float
    let label: String
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        HStack(spacing: 6) {
            Circle().fill(color).frame(width: 10, height: 10)
            VStack(alignment: .leading, spacing: 0) {
                Text("\(Int(value))g").font(.caption.weight(.semibold)).foregroundStyle(colors.textPrimary)
                Text(label).font(.caption2).foregroundStyle(colors.textSecondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
