import SwiftUI
import CalStuffShared

/// Mirrors Android's `AddMealScreen` — a fake "AI" description parser plus a manual-entry cart of
/// items, each independently editable/removable before Save writes one Firestore meal document
/// per item. See `AddMealViewModel` for the ported mock-parsing logic and save behavior.
struct AddMealView: View {
    @StateObject private var viewModel: AddMealViewModel
    @Environment(\.calStuffColors) private var colors
    @Environment(\.dismiss) private var dismiss
    @FocusState private var descriptionFocused: Bool

    init(store: NutritionStore, day: Int32) {
        _viewModel = StateObject(wrappedValue: AddMealViewModel(store: store, day: day))
    }

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        descriptionSection
                        itemsSection
                        if viewModel.showAddManualForm {
                            manualAddForm
                                .transition(.opacity.combined(with: .move(edge: .top)))
                        }
                    }
                    .padding(20)
                    .padding(.bottom, 100)
                }
                .background(colors.background.ignoresSafeArea())

                bottomBar
            }
            .animation(.easeInOut(duration: 0.2), value: viewModel.showAddManualForm)
            .navigationTitle("Add Meal")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
            .alert("Couldn't save meal", isPresented: Binding(
                get: { viewModel.saveError != nil },
                set: { if !$0 { viewModel.saveError = nil } }
            )) {
                Button("OK") { viewModel.saveError = nil }
            } message: {
                Text(viewModel.saveError ?? "")
            }
        }
    }

    private var descriptionSection: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Describe what you ate").font(.subheadline.weight(.semibold)).foregroundStyle(colors.textPrimary)
            Text("AI will estimate calories & macros for each item").font(.caption2).foregroundStyle(colors.textSecondary)

            TextEditor(text: Binding(
                get: { viewModel.aiDescription },
                set: { viewModel.updateDescription($0) }
            ))
            .focused($descriptionFocused)
            .scrollContentBackground(.hidden)
            .frame(height: 130)
            .padding(8)
            .background(colors.surface)
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(descriptionFocused ? colors.highlight : colors.divider, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(alignment: .topLeading) {
                if viewModel.aiDescription.isEmpty {
                    Text("e.g. 2 roti with dal, a bowl of rice, and buttermilk")
                        .font(.caption)
                        .foregroundStyle(colors.textSecondary)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 16)
                        .allowsHitTesting(false)
                }
            }

            Text("\(viewModel.wordCount) / 300 words")
                .font(.caption2)
                .foregroundStyle(viewModel.wordCount > 280 ? colors.error : colors.textSecondary)
        }
    }

    @ViewBuilder
    private var itemsSection: some View {
        if viewModel.foodItems.isEmpty {
            VStack(spacing: 8) {
                Button(action: { viewModel.showAddManualForm = true }) {
                    Image(systemName: "plus")
                        .foregroundStyle(colors.onHighlight)
                        .frame(width: 48, height: 48)
                        .background(colors.highlight)
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
                Text("Add item manually").font(.caption).foregroundStyle(colors.textSecondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 24)
        } else {
            VStack(alignment: .leading, spacing: 10) {
                Text("\(viewModel.foodItems.count) item\(viewModel.foodItems.count > 1 ? "s" : "") added")
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(colors.textPrimary)

                VStack(alignment: .leading, spacing: 14) {
                    ForEach(Array(viewModel.foodItems.enumerated()), id: \.element.id) { index, item in
                        FoodItemRow(
                            item: item,
                            onEdit: { viewModel.replaceItem($0) },
                            onRemove: { viewModel.removeItem(item) }
                        )
                        if index < viewModel.foodItems.count - 1 {
                            Divider().overlay(colors.divider)
                        }
                    }

                    Divider().overlay(colors.divider)
                    HStack {
                        Text("Total").font(.subheadline.weight(.bold)).foregroundStyle(colors.textPrimary)
                        Spacer()
                        Text("\(viewModel.totalCalories) kcal").font(.subheadline.weight(.bold)).foregroundStyle(colors.error)
                    }
                    HStack(spacing: 16) {
                        macroBadge("P", "\(Int(viewModel.totalProtein))g", colors.highlight)
                        macroBadge("C", "\(Int(viewModel.totalCarbs))g", colors.error)
                        macroBadge("F", "\(Int(viewModel.totalFat))g", colors.textPrimary)
                    }

                    Divider().overlay(colors.divider)
                    Button(action: { viewModel.showAddManualForm = true }) {
                        HStack(spacing: 6) {
                            Image(systemName: "plus").font(.caption)
                            Text("Add another item").font(.caption.weight(.semibold))
                        }
                        .foregroundStyle(colors.highlight)
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.plain)
                }
                .padding(16)
                .background(colors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            }
        }
    }

    private var manualAddForm: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Add Item").font(.subheadline.weight(.semibold)).foregroundStyle(colors.textPrimary)

            MealItemFormFields(
                name: $viewModel.manualName,
                calories: $viewModel.manualCalories,
                protein: $viewModel.manualProtein,
                carbs: $viewModel.manualCarbs,
                fat: $viewModel.manualFat,
                mealType: $viewModel.manualMealType,
                allowDecimalMacros: true,
                nameLabel: "Food name"
            )

            if !viewModel.manualError.isEmpty {
                Text(viewModel.manualError).font(.caption2).foregroundStyle(colors.error)
            }

            HStack(spacing: 8) {
                Button("Cancel") {
                    viewModel.showAddManualForm = false
                    viewModel.manualError = ""
                }
                .buttonStyle(.bordered)
                .frame(maxWidth: .infinity)

                Button("Add") { viewModel.addManualItem() }
                    .buttonStyle(.borderedProminent)
                    .tint(colors.highlight)
                    .disabled(viewModel.manualName.trimmingCharacters(in: .whitespaces).isEmpty || viewModel.manualCalories.isEmpty)
                    .frame(maxWidth: .infinity)
            }
        }
        .padding(16)
        .background(colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
    }

    private var bottomBar: some View {
        VStack(spacing: 8) {
            if !viewModel.aiDescription.trimmingCharacters(in: .whitespaces).isEmpty {
                Button(action: {
                    descriptionFocused = false
                    viewModel.analyzeWithAI()
                }) {
                    HStack(spacing: 8) {
                        if viewModel.isAnalyzing {
                            ProgressView().tint(colors.onHighlight)
                        } else {
                            Image(systemName: "sparkle.magnifyingglass")
                        }
                        Text(viewModel.isAnalyzing ? "Analyzing..." : "Analyze with AI")
                            .font(.subheadline.weight(.semibold))
                    }
                    .foregroundStyle(colors.onHighlight)
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                }
                .buttonStyle(.plain)
                .background(colors.highlight)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .disabled(viewModel.isAnalyzing)
            }

            Button(action: { viewModel.save(onSuccess: { dismiss() }) }) {
                Text(viewModel.canSave ? "Save \(viewModel.foodItems.count) Item\(viewModel.foodItems.count > 1 ? "s" : "")" : "Add items to save")
                    .font(.body.weight(.semibold))
                    .frame(maxWidth: .infinity)
                    .frame(height: 52)
            }
            .buttonStyle(.plain)
            .foregroundStyle(viewModel.canSave ? colors.onInverseSurface : colors.textSecondary)
            .background(viewModel.canSave ? colors.inverseSurface : colors.divider)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .disabled(!viewModel.canSave || viewModel.isSaving)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 10)
        .background(colors.background)
    }

    private func macroBadge(_ label: String, _ value: String, _ color: Color) -> some View {
        HStack(spacing: 4) {
            RoundedRectangle(cornerRadius: 2).fill(color).frame(width: 8, height: 8)
            Text("\(label): \(value)").font(.caption2).foregroundStyle(colors.textSecondary)
        }
    }
}
