import SwiftUI
import CalStuffShared

/// Mirrors Android's `MealsScreen` — today's meals grouped by type (Android's own
/// `editableWindowDays` is hardcoded to 1 everywhere, so only today is ever shown/editable in the
/// real app; no date-chip UI needed to match actual behavior), each with inline Edit/Delete.
struct MealsView: View {
    @ObservedObject var store: NutritionStore
    @Environment(\.calStuffColors) private var colors
    @State private var showingAddMeal = false
    @State private var editingMealId: String?

    private var mealsByType: [(type: String, meals: [MealEntry])] {
        let meals = store.todayNutrition?.meals ?? []
        let order = ["BREAKFAST", "LUNCH", "DINNER", "SNACKS"]
        return order.compactMap { type in
            let matching = meals.filter { $0.mealType == type }
            return matching.isEmpty ? nil : (type, matching)
        }
    }

    private var totalCalories: Int {
        (store.todayNutrition?.meals ?? []).reduce(0) { $0 + Int($1.calories) }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    header

                    if mealsByType.isEmpty {
                        emptyState
                    } else {
                        ForEach(mealsByType, id: \.type) { group in
                            VStack(alignment: .leading, spacing: 10) {
                                HStack(spacing: 8) {
                                    Circle().fill(MealTypeStyle.color(forWireValue: group.type, colors)).frame(width: 8, height: 8)
                                    Text(MealTypeStyle.label(forWireValue: group.type))
                                        .font(.caption.weight(.semibold))
                                        .foregroundStyle(colors.textSecondary)
                                }
                                ForEach(group.meals, id: \.id) { meal in
                                    EditableMealCard(
                                        meal: meal,
                                        isEditing: editingMealId == meal.id,
                                        onEditTapped: {
                                            editingMealId = editingMealId == meal.id ? nil : meal.id
                                        },
                                        onDelete: {
                                            Task { try? await store.deleteMeal(meal.id, from: store.today.day) }
                                            if editingMealId == meal.id { editingMealId = nil }
                                        },
                                        onSaveEdit: { updated in
                                            Task { try? await store.updateMeal(updated, in: store.today.day) }
                                            editingMealId = nil
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                .padding(20)
                .padding(.bottom, 60)
            }
            .background(colors.background.ignoresSafeArea())
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(action: { showingAddMeal = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingAddMeal) {
                AddMealView(store: store, day: store.today.day)
            }
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Today's Meals").font(.title.bold()).foregroundStyle(colors.textPrimary)
            if store.todayNutrition != nil {
                HStack(spacing: 4) {
                    Text("\(totalCalories)").fontWeight(.semibold).foregroundStyle(colors.textPrimary)
                    Text("/ \(store.calorieGoal) kcal").foregroundStyle(colors.textSecondary)
                }
                .font(.subheadline)
            }
        }
    }

    private var emptyState: some View {
        VStack(spacing: 8) {
            Image(systemName: "fork.knife").font(.largeTitle).foregroundStyle(colors.textSecondary)
            Text("No meals logged").font(.headline).foregroundStyle(colors.textPrimary)
            Text("Tap + to add your first meal").font(.caption).foregroundStyle(colors.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
    }
}

private struct EditableMealCard: View {
    let meal: MealEntry
    let isEditing: Bool
    let onEditTapped: () -> Void
    let onDelete: () -> Void
    let onSaveEdit: (MealEntry) -> Void
    @Environment(\.calStuffColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(meal.name).font(.headline).foregroundStyle(colors.textPrimary)
                    Text(meal.time).font(.caption).foregroundStyle(colors.textSecondary)
                }
                Spacer()
                Button(action: onEditTapped) {
                    Image(systemName: "pencil").foregroundStyle(isEditing ? colors.highlight : colors.textSecondary)
                }
                Button(action: onDelete) {
                    Image(systemName: "trash").foregroundStyle(colors.error)
                }
            }
            HStack {
                Text("\(meal.calories) kcal").font(.subheadline.weight(.bold)).foregroundStyle(colors.textPrimary)
                Spacer()
                HStack(spacing: 6) {
                    macroPill("P", "\(Int(meal.protein))g", colors.highlight)
                    macroPill("C", "\(Int(meal.carbs))g", colors.error)
                    macroPill("F", "\(Int(meal.fat))g", colors.textPrimary)
                }
            }
            if isEditing {
                InlineEditForm(meal: meal, onSave: onSaveEdit)
                    .transition(.asymmetric(insertion: .opacity.combined(with: .move(edge: .top)), removal: .opacity))
            }
        }
        .padding(16)
        .background(colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .animation(.easeInOut(duration: 0.2), value: isEditing)
    }

    private func macroPill(_ label: String, _ value: String, _ color: Color) -> some View {
        HStack(spacing: 2) {
            Text(label).font(.caption2.weight(.bold)).foregroundStyle(color)
            Text(value).font(.caption2.weight(.medium)).foregroundStyle(colors.textPrimary.opacity(0.7))
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(color.opacity(0.12))
        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
    }
}

/// Matches Android's `InlineEditForm` exactly: name/calories/protein/carbs/fat, digits-only
/// numeric fields (no decimal point — unlike AddMeal's manual form), no meal-type change.
private struct InlineEditForm: View {
    let meal: MealEntry
    let onSave: (MealEntry) -> Void
    @Environment(\.calStuffColors) private var colors

    @State private var name: String
    @State private var calories: String
    @State private var protein: String
    @State private var carbs: String
    @State private var fat: String

    init(meal: MealEntry, onSave: @escaping (MealEntry) -> Void) {
        self.meal = meal
        self.onSave = onSave
        _name = State(initialValue: meal.name)
        _calories = State(initialValue: "\(meal.calories)")
        _protein = State(initialValue: "\(Int(meal.protein))")
        _carbs = State(initialValue: "\(Int(meal.carbs))")
        _fat = State(initialValue: "\(Int(meal.fat))")
    }

    var body: some View {
        VStack(spacing: 8) {
            field("Name", text: $name)
            HStack(spacing: 8) {
                field("Cal", text: $calories, digitsOnly: true)
                field("Protein", text: $protein, digitsOnly: true)
            }
            HStack(spacing: 8) {
                field("Carbs", text: $carbs, digitsOnly: true)
                field("Fat", text: $fat, digitsOnly: true)
            }
            Button(action: save) {
                Text("Save")
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(colors.onHighlight)
                    .frame(maxWidth: .infinity)
                    .frame(height: 40)
            }
            .buttonStyle(.plain)
            .background(colors.highlight)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
        .padding(.top, 12)
    }

    private func field(_ label: String, text: Binding<String>, digitsOnly: Bool = false) -> some View {
        TextField(label, text: digitsOnly ? Binding(
            get: { text.wrappedValue },
            set: { text.wrappedValue = $0.filter(\.isNumber) }
        ) : text)
        .keyboardType(digitsOnly ? .numberPad : .default)
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(colors.surfaceVariant)
        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
    }

    private func save() {
        onSave(MealEntry(
            id: meal.id,
            name: name,
            mealType: meal.mealType,
            calories: Int32(calories) ?? meal.calories,
            protein: Float(protein) ?? meal.protein,
            carbs: Float(carbs) ?? meal.carbs,
            fat: Float(fat) ?? meal.fat,
            time: meal.time
        ))
    }
}
