import SwiftUI
import UIKit

/// Mirrors Android `CalAuraOutlinedTextField`: 16pt corners, light fill, soft border.
struct CalAuraOutlinedTextField: View {
    let title: String
    let placeholder: String
    @Binding var text: String
    var keyboard: UIKeyboardType = .default
    var textContentType: UITextContentType?
    var enabled: Bool = true

    init(
        title: String,
        placeholder: String,
        text: Binding<String>,
        keyboard: UIKeyboardType = .default,
        textContentType: UITextContentType? = nil,
        enabled: Bool = true
    ) {
        self.title = title
        self.placeholder = placeholder
        self._text = text
        self.keyboard = keyboard
        self.textContentType = textContentType
        self.enabled = enabled
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.subheadline)
                .foregroundStyle(LoginDesignTokens.otpHint)
            TextField(placeholder, text: $text)
                .keyboardType(keyboard)
                .textContentType(textContentType)
                .disabled(!enabled)
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
                .background(LoginDesignTokens.auraFieldFill)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .stroke(LoginDesignTokens.auraBorder, lineWidth: 1)
                )
        }
    }
}
