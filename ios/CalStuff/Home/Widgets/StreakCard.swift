import SwiftUI

/// Mirrors Android's `StreakCard.kt` — big streak number or "Start your streak" empty state,
/// with a flame graphic in the bottom-right corner. Android uses a Lottie fire animation (lit)
/// vs. a greyscale GIF (unlit); neither asset exists in the iOS project, so this substitutes a
/// colored-vs-greyed `flame.fill` SF Symbol — a deliberate, noted simplification, not full parity.
struct StreakCard: View {
    let streakDays: Int
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack(alignment: .leading, spacing: 4) {
                if streakDays > 0 {
                    Text("day streak")
                        .font(.caption.weight(.medium))
                        .foregroundStyle(colors.textSecondary)
                    Text("\(streakDays)")
                        .font(.system(size: 44, weight: .heavy))
                        .foregroundStyle(colors.textPrimary)
                } else {
                    Text("Start your\nstreak")
                        .font(.headline)
                        .foregroundStyle(colors.textPrimary)
                    Text("Log a meal today\nto get started")
                        .font(.caption)
                        .foregroundStyle(colors.textSecondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Image(systemName: "flame.fill")
                .font(.system(size: 64))
                .foregroundStyle(streakDays > 0 ? colors.accent : colors.divider)
                .offset(x: 14, y: 14)
        }
        .padding(16)
        .frame(maxWidth: .infinity, minHeight: 150, alignment: .topLeading)
        .background(colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .clipped()
    }
}
