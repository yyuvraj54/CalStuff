import SwiftUI

/// Mirrors Android `CalOrDivider`.
struct CalOrDivider: View {
    let text: String

    var body: some View {
        HStack(spacing: 0) {
            line
            Text(text)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(LoginDesignTokens.orLabel)
                .padding(.horizontal, 16)
            line
        }
        .padding(.vertical, 8)
    }

    private var line: some View {
        Rectangle()
            .fill(LoginDesignTokens.dividerLine)
            .frame(height: 1)
            .frame(maxWidth: .infinity)
    }
}
