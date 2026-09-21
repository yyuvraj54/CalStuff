import SwiftUI
import CalStuffShared

/// Mirrors Android's `OnboardingFormScreen` — 6-step wizard (name, age, gender, height, weight,
/// activity), same validation rules, same calorie-goal computation on finish.
struct OnboardingView: View {
    @StateObject private var viewModel: OnboardingViewModel
    @Environment(\.calStuffColors) private var colors
    @FocusState private var fieldFocused: Bool
    let onFinished: () -> Void

    init(dependencies: AppDependencies, onFinished: @escaping () -> Void) {
        _viewModel = StateObject(wrappedValue: OnboardingViewModel(userProfileRepository: dependencies.userProfileRepository))
        self.onFinished = onFinished
    }

    var body: some View {
        VStack(spacing: 0) {
            header
            Spacer(minLength: 12)
            stepContent
                .padding(.horizontal, 24)
            Spacer()
            footer
        }
        .background(colors.background.ignoresSafeArea())
        .alert("Couldn't save profile", isPresented: Binding(
            get: { viewModel.saveError != nil },
            set: { if !$0 { viewModel.clearSaveError() } }
        )) {
            Button("OK") { viewModel.clearSaveError() }
        } message: {
            Text(viewModel.saveError ?? "")
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 12) {
            if viewModel.stepIndex > 0 {
                Button(action: viewModel.goBack) {
                    Image(systemName: "chevron.left")
                        .foregroundStyle(colors.textPrimary)
                }
            }
            HStack(spacing: 6) {
                ForEach(0..<OnboardingViewModel.totalSteps, id: \.self) { index in
                    Capsule()
                        .fill(index <= viewModel.stepIndex ? colors.accent : colors.divider)
                        .frame(height: 4)
                }
            }
        }
        .padding(.horizontal, 24)
        .padding(.top, 16)
    }

    @ViewBuilder
    private var stepContent: some View {
        switch viewModel.stepIndex {
        case 0:
            StepText(title: "What's your name?", subtitle: "So we know what to call you.") {
                OnboardingTextField(text: $viewModel.name, placeholder: "Your name", focused: $fieldFocused)
            }
        case 1:
            StepText(title: "How old are you?", subtitle: "Used to estimate your daily calorie needs.") {
                OnboardingTextField(text: $viewModel.ageText, placeholder: "Age", keyboard: .numberPad, focused: $fieldFocused)
            }
        case 2:
            StepText(title: "What's your gender?", subtitle: "Used in the calorie-goal formula.") {
                VStack(spacing: 12) {
                    genderOption(.male, label: "Male")
                    genderOption(.female, label: "Female")
                    genderOption(.other, label: "Other")
                    genderOption(.preferNotSay, label: "Prefer not to say")
                }
            }
        case 3:
            StepText(title: "How tall are you?", subtitle: "In centimeters.") {
                OnboardingTextField(text: $viewModel.heightCmText, placeholder: "Height (cm)", keyboard: .numberPad, focused: $fieldFocused)
            }
        case 4:
            StepText(title: "What's your weight?", subtitle: "In kilograms.") {
                OnboardingTextField(text: $viewModel.weightKgText, placeholder: "Weight (kg)", keyboard: .numberPad, focused: $fieldFocused)
            }
        case 5:
            StepText(title: "How active are you?", subtitle: "Used to estimate your daily calorie needs.") {
                VStack(spacing: 12) {
                    activityOption(.sedentary, label: "Sedentary", detail: "Little to no exercise")
                    activityOption(.light, label: "Light", detail: "1-3 days/week")
                    activityOption(.moderate, label: "Moderate", detail: "3-5 days/week")
                    activityOption(.active, label: "Active", detail: "6-7 days/week")
                    activityOption(.veryActive, label: "Very Active", detail: "Physical job or 2x/day")
                }
            }
        default:
            EmptyView()
        }
    }

    private func genderOption(_ value: Gender, label: String) -> some View {
        OnboardingOptionRow(label: label, isSelected: viewModel.gender == value) {
            viewModel.gender = value
        }
    }

    private func activityOption(_ value: ActivityLevel, label: String, detail: String) -> some View {
        OnboardingOptionRow(label: label, detail: detail, isSelected: viewModel.activity == value) {
            viewModel.activity = value
        }
    }

    private var footer: some View {
        Button(action: handleContinue) {
            Text(viewModel.stepIndex == OnboardingViewModel.totalSteps - 1 ? "Finish" : "Continue")
                .fontWeight(.semibold)
                .frame(maxWidth: .infinity)
                .frame(height: 52)
        }
        .buttonStyle(.plain)
        .foregroundStyle(colors.onHighlight)
        .background(viewModel.isCurrentStepValid ? colors.highlight : colors.divider)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .disabled(!viewModel.isCurrentStepValid || viewModel.isSaving)
        .padding(24)
    }

    private func handleContinue() {
        fieldFocused = false
        switch viewModel.onContinue() {
        case .advanced, .invalid:
            break
        case .finished:
            viewModel.saveProfile(onSuccess: onFinished)
        }
    }
}

private struct StepText<Content: View>: View {
    let title: String
    let subtitle: String
    @ViewBuilder let content: Content
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            VStack(alignment: .leading, spacing: 6) {
                Text(title)
                    .font(.title2.bold())
                    .foregroundStyle(colors.textPrimary)
                Text(subtitle)
                    .font(.subheadline)
                    .foregroundStyle(colors.textSecondary)
            }
            content
        }
    }
}

private struct OnboardingTextField: View {
    @Binding var text: String
    let placeholder: String
    var keyboard: UIKeyboardType = .default
    var focused: FocusState<Bool>.Binding

    @Environment(\.calStuffColors) private var colors

    var body: some View {
        TextField(placeholder, text: $text, prompt: Text(placeholder).foregroundColor(colors.textSecondary))
            .foregroundStyle(colors.textPrimary)
            .tint(colors.accent)
            .keyboardType(keyboard)
            .focused(focused)
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(colors.surfaceVariant)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
}

private struct OnboardingOptionRow: View {
    let label: String
    var detail: String?
    let isSelected: Bool
    let onSelect: () -> Void

    @Environment(\.calStuffColors) private var colors

    var body: some View {
        Button(action: onSelect) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(label)
                        .fontWeight(.semibold)
                        .foregroundStyle(isSelected ? colors.onAccentSoft : colors.textPrimary)
                    if let detail {
                        Text(detail)
                            .font(.caption)
                            .foregroundStyle(colors.textSecondary)
                    }
                }
                Spacer()
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundStyle(colors.accent)
                }
            }
            .padding(16)
            .background(isSelected ? colors.accentSoft : colors.surfaceVariant)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}
