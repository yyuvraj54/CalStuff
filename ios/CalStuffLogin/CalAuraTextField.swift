import SwiftUI
import UIKit

/// Fields that participate in keyboard focus + scroll-to-visible on the login screen.
enum LoginTextFocus: Hashable {
    case phone
    case otp
}

/// Mirrors Android `CalAuraOutlinedTextField`.
struct CalAuraOutlinedTextField: View {
    let title: String
    let placeholder: String
    @Binding var text: String
    var keyboard: UIKeyboardType = .default
    var textContentType: UITextContentType?
    var enabled: Bool = true
    var focusTag: LoginTextFocus
    @FocusState.Binding var focusedField: LoginTextFocus?

    init(
        title: String,
        placeholder: String,
        text: Binding<String>,
        keyboard: UIKeyboardType = .default,
        textContentType: UITextContentType? = nil,
        enabled: Bool = true,
        focusTag: LoginTextFocus,
        focusedField: FocusState<LoginTextFocus?>.Binding
    ) {
        self.title = title
        self.placeholder = placeholder
        self._text = text
        self.keyboard = keyboard
        self.textContentType = textContentType
        self.enabled = enabled
        self.focusTag = focusTag
        self._focusedField = focusedField
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.subheadline)
                .foregroundStyle(LoginDesignTokens.otpHint)
            TextField(
                "",
                text: $text,
                prompt: Text(placeholder).foregroundStyle(LoginDesignTokens.fieldPlaceholder)
            )
            .foregroundStyle(LoginDesignTokens.fieldText)
            .tint(LoginDesignTokens.fieldCursor)
            .keyboardType(keyboard)
            .textContentType(textContentType)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .disabled(!enabled)
            .focused($focusedField, equals: focusTag)
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
