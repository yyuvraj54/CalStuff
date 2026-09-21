import SwiftUI

extension BmiCategory {
    func color(_ colors: CalStuffColors) -> Color {
        switch self {
        case .underweight: return colors.info
        case .normal: return colors.success
        case .overweight: return colors.highlight
        case .obese: return colors.error
        }
    }
}

/// Mirrors Android's `BmiCard.kt` + `BmiGauge.kt` — semicircle gauge with 4 colored zones and an
/// animated needle, category pill, and a height/weight footer.
struct BmiCard: View {
    let config: BmiConfig
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text("Body Mass Index").font(.headline).foregroundStyle(colors.textPrimary)
                Spacer()
                Text(config.category.rawValue)
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(config.category.color(colors))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(config.category.color(colors).opacity(0.15))
                    .clipShape(Capsule())
            }
            HStack {
                Spacer()
                BmiGauge(bmi: config.bmiValue, category: config.category)
                Spacer()
            }
            VStack(alignment: .leading, spacing: 4) {
                HStack(alignment: .firstTextBaseline, spacing: 6) {
                    Text(String(format: "%.1f", config.bmiValue))
                        .font(.title.bold())
                        .foregroundStyle(config.category.color(colors))
                    Text(config.category.rawValue).font(.subheadline).foregroundStyle(colors.textSecondary)
                }
                Text(config.healthNote).font(.caption).foregroundStyle(colors.textSecondary)
            }
            Divider().overlay(colors.divider)
            HStack {
                statPair("Height", "\(Int(config.heightCm)) cm")
                Spacer()
                statPair("Weight", "\(Int(config.weightKg)) kg")
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }

    private func statPair(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.caption2).foregroundStyle(colors.textSecondary)
            Text(value).font(.subheadline.weight(.semibold)).foregroundStyle(colors.textPrimary)
        }
    }
}

struct BmiGauge: View {
    let bmi: Float
    let category: BmiCategory
    @Environment(\.calStuffColors) private var colors

    private let minBmi: Double = 10
    private let maxBmi: Double = 40

    private var zones: [(lower: Double, upper: Double, color: Color)] {
        [(10, 18.5, colors.info), (18.5, 25, colors.success), (25, 30, colors.highlight), (30, 40, colors.error)]
    }

    /// Maps a BMI value to an angle in the top-bowing semicircle: min→π (left), mid→1.5π (up),
    /// max→2π (right). Screen-space convention: x = cx + r·cos(θ), y = cy + r·sin(θ).
    private func angle(for value: Double) -> Double {
        let clamped = min(max(value, minBmi), maxBmi)
        let ratio = (clamped - minBmi) / (maxBmi - minBmi)
        return .pi + ratio * .pi
    }

    private func arcPath(center: CGPoint, radius: CGFloat, from startAngle: Double, to endAngle: Double) -> Path {
        var path = Path()
        let steps = 24
        for i in 0...steps {
            let t = Double(i) / Double(steps)
            let angle = startAngle + (endAngle - startAngle) * t
            let point = CGPoint(x: center.x + radius * cos(angle), y: center.y + radius * sin(angle))
            if i == 0 { path.move(to: point) } else { path.addLine(to: point) }
        }
        return path
    }

    var body: some View {
        Canvas { context, size in
            let center = CGPoint(x: size.width / 2, y: size.height)
            let radius = min(size.width / 2, size.height) - 12

            for zone in zones {
                let gap = 0.02
                let path = arcPath(center: center, radius: radius, from: angle(for: zone.lower) + gap, to: angle(for: zone.upper) - gap)
                context.stroke(path, with: .color(zone.color), style: StrokeStyle(lineWidth: 16, lineCap: .round))
            }

            let needleAngle = angle(for: Double(bmi))
            let needleEnd = CGPoint(x: center.x + (radius - 22) * cos(needleAngle), y: center.y + (radius - 22) * sin(needleAngle))
            var needlePath = Path()
            needlePath.move(to: center)
            needlePath.addLine(to: needleEnd)
            context.stroke(needlePath, with: .color(colors.textPrimary), style: StrokeStyle(lineWidth: 3, lineCap: .round))
            context.fill(Path(ellipseIn: CGRect(x: needleEnd.x - 5, y: needleEnd.y - 5, width: 10, height: 10)), with: .color(category.color(colors)))
            context.fill(Path(ellipseIn: CGRect(x: center.x - 6, y: center.y - 6, width: 12, height: 12)), with: .color(colors.textPrimary))
        }
        .frame(width: 210, height: 108)
        .animation(.spring(response: 0.6, dampingFraction: 0.75), value: bmi)
    }
}
