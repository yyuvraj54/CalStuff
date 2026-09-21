import SwiftUI
import CalStuffShared

/// Meal-type pills + name/calories/macro fields — the shape shared by the manual "Add Item" form
/// and each `FoodItemRow`'s inline edit form in Android's `AddMealScreen.kt` (there, duplicated
/// as two separate composables with identical layout; here, one reusable view since Swift makes
/// that free).
///
/// `allowDecimalMacros` mirrors a real difference in Android, not an oversight: the manual-add
/// form accepts a decimal point in protein/carbs/fat (`c.isDigit() || c == '.'`), while the
/// per-item inline-edit form in `FoodItemRow` is digits-only for every field. Calories is always
/// digits-only in both.
struct MealItemFormFields: View {
    @Binding var name: String
    @Binding var calories: String
    @Binding var protein: String
    @Binding var carbs: String
    @Binding var fat: String
    @Binding var mealType: MealType
    var allowDecimalMacros: Bool = true
    var nameLabel: String = "Food name"

    @Environment(\.calStuffColors) private var colors

    private static let allTypes: [MealType] = [.breakfast, .lunch, .dinner, .snacks]

    var body: some View {
        VStack(spacing: 8) {
            HStack(spacing: 6) {
                ForEach(Self.allTypes, id: \.wireValue) { type in
                    MealTypePill(type: type, isSelected: mealType == type) { mealType = type }
                }
            }
            filteredField(nameLabel, text: $name, filter: nil)
            filteredField("Calories (kcal)", text: $calories, filter: { $0.filter(\.isNumber) }, keyboard: .numberPad)
            HStack(spacing: 8) {
                filteredField("Protein", text: $protein, filter: macroFilter, keyboard: allowDecimalMacros ? .decimalPad : .numberPad)
                filteredField("Carbs", text: $carbs, filter: macroFilter, keyboard: allowDecimalMacros ? .decimalPad : .numberPad)
                filteredField("Fat", text: $fat, filter: macroFilter, keyboard: allowDecimalMacros ? .decimalPad : .numberPad)
            }
        }
    }

    private var macroFilter: (String) -> String {
        allowDecimalMacros
            ? { $0.filter { $0.isNumber || $0 == "." } }
            : { $0.filter(\.isNumber) }
    }

    private func filteredField(_ label: String, text: Binding<String>, filter: ((String) -> String)?, keyboard: UIKeyboardType = .default) -> some View {
        TextField(label, text: Binding(
            get: { text.wrappedValue },
            set: { text.wrappedValue = filter?($0) ?? $0 }
        ))
        .keyboardType(keyboard)
        .foregroundStyle(colors.textPrimary)
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(colors.surfaceVariant)
        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
    }
}

/// Mirrors Android's `MealTypePill` — filled with the type's color when selected (Dinner's
/// selected-state text flips to `onInverseSurface` since its color *is* `inverseSurface`, matching
/// Android's specific check), outlined in that color otherwise.
struct MealTypePill: View {
    let type: MealType
    let isSelected: Bool
    let onSelect: () -> Void
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        let color = MealTypeStyle.color(for: type, colors)
        let textColor: Color = isSelected ? (type == .dinner ? colors.onInverseSurface : colors.inverseSurface) : color

        Button(action: onSelect) {
            Text(MealTypeStyle.label(for: type))
                .font(.system(size: 10, weight: .semibold))
                .lineLimit(1)
                .minimumScaleFactor(0.8)
                .foregroundStyle(textColor)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(isSelected ? color : Color.clear)
                .overlay(
                    RoundedRectangle(cornerRadius: 20, style: .continuous)
                        .stroke(isSelected ? Color.clear : color, lineWidth: 1.5)
                )
                .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}
