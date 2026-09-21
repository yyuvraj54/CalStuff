import SwiftUI
import CalStuffShared

/// Mirrors Android's `FoodItemRow` — display state (name, meal-type pill, calories, macro
/// badges, Edit/Remove text links) plus an inline edit form when tapped, with its own local
/// validation error line.
struct FoodItemRow: View {
    let item: FoodItem
    let onEdit: (FoodItem) -> Void
    let onRemove: () -> Void

    @State private var isEditing = false
    @State private var name: String
    @State private var calories: String
    @State private var protein: String
    @State private var carbs: String
    @State private var fat: String
    @State private var mealType: MealType
    @State private var error = ""

    @Environment(\.calStuffColors) private var colors

    init(item: FoodItem, onEdit: @escaping (FoodItem) -> Void, onRemove: @escaping () -> Void) {
        self.item = item
        self.onEdit = onEdit
        self.onRemove = onRemove
        _name = State(initialValue: item.name)
        _calories = State(initialValue: "\(item.calories)")
        _protein = State(initialValue: "\(Int(item.protein))")
        _carbs = State(initialValue: "\(Int(item.carbs))")
        _fat = State(initialValue: "\(Int(item.fat))")
        _mealType = State(initialValue: item.mealType)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if isEditing {
                MealItemFormFields(
                    name: $name, calories: $calories, protein: $protein, carbs: $carbs, fat: $fat,
                    mealType: $mealType, allowDecimalMacros: false, nameLabel: "Name"
                )
                if !error.isEmpty {
                    Text(error).font(.caption2).foregroundStyle(colors.error)
                }
                HStack(spacing: 8) {
                    Button("Cancel") { isEditing = false; error = "" }
                        .buttonStyle(.bordered)
                        .frame(maxWidth: .infinity)
                    Button("Save", action: save)
                        .buttonStyle(.borderedProminent)
                        .tint(colors.highlight)
                        .frame(maxWidth: .infinity)
                }
            } else {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text(item.name).font(.subheadline.weight(.semibold)).foregroundStyle(colors.textPrimary)
                        Text(MealTypeStyle.label(for: item.mealType))
                            .font(.system(size: 9, weight: .semibold))
                            .foregroundStyle(MealTypeStyle.color(for: item.mealType, colors))
                            .padding(.horizontal, 8).padding(.vertical, 2)
                            .background(MealTypeStyle.color(for: item.mealType, colors).opacity(0.12))
                            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                    }
                    Spacer()
                    Text("\(item.calories) kcal").font(.subheadline.weight(.bold)).foregroundStyle(colors.textSecondary)
                }
                HStack(spacing: 12) {
                    macroBadge("P", "\(Int(item.protein))g", colors.highlight)
                    macroBadge("C", "\(Int(item.carbs))g", colors.error)
                    macroBadge("F", "\(Int(item.fat))g", colors.textPrimary)
                }
                HStack(spacing: 12) {
                    Button("Edit") { isEditing = true }
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(colors.highlight)
                    Button("Remove", action: onRemove)
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(colors.error)
                }
            }
        }
        .animation(.easeInOut(duration: 0.2), value: isEditing)
    }

    private func macroBadge(_ label: String, _ value: String, _ color: Color) -> some View {
        HStack(spacing: 4) {
            RoundedRectangle(cornerRadius: 2).fill(color).frame(width: 8, height: 8)
            Text("\(label): \(value)").font(.caption2).foregroundStyle(colors.textSecondary)
        }
    }

    private func save() {
        var candidate = FoodItem(
            name: name.trimmingCharacters(in: .whitespaces).isEmpty ? item.name : name.trimmingCharacters(in: .whitespaces),
            calories: Int32(calories) ?? item.calories,
            protein: Float(protein) ?? item.protein,
            carbs: Float(carbs) ?? item.carbs,
            fat: Float(fat) ?? item.fat,
            mealType: mealType
        )
        let errors = MealValidationKt.validateFoodItem(name: candidate.name, calories: candidate.calories, protein: candidate.protein, carbs: candidate.carbs, fat: candidate.fat)
        guard errors.isEmpty else {
            error = errors.first ?? ""
            return
        }
        // `replaceItem` in the view model matches by id, so the edited copy must keep it.
        candidate.id = item.id
        onEdit(candidate)
        isEditing = false
        error = ""
    }
}
