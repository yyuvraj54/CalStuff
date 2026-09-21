import SwiftUI
import CalStuffShared

/// Mirrors Android's `ui/model/MealType.kt` (`label`) + `ui/theme/MealTypeColor.kt`
/// (`themeColor`) exactly — one place for both so every screen showing a meal type
/// (Meals, Logs, AddMeal) stays consistent.
enum MealTypeStyle {
    static func label(for mealType: MealType) -> String {
        switch mealType {
        case .breakfast: return "Breakfast"
        case .lunch: return "Lunch"
        case .dinner: return "Dinner"
        case .snacks: return "Snacks / Misc"
        default: return "Snacks / Misc"
        }
    }

    /// Deliberately theme-stable colors (not `textPrimary`-relative) — some call sites use this
    /// as a filled chip background paired with a fixed contrasting text color, matching Android's
    /// own comment on `MealTypeColor.kt` about why these don't flip with light/dark theme.
    static func color(for mealType: MealType, _ colors: CalStuffColors) -> Color {
        switch mealType {
        case .breakfast: return colors.highlight
        case .lunch: return colors.error
        case .dinner: return colors.inverseSurface
        case .snacks: return colors.success
        default: return colors.success
        }
    }

    static func label(forWireValue wireValue: String) -> String {
        switch wireValue {
        case MealType.breakfast.wireValue: return label(for: .breakfast)
        case MealType.lunch.wireValue: return label(for: .lunch)
        case MealType.dinner.wireValue: return label(for: .dinner)
        default: return label(for: .snacks)
        }
    }

    static func color(forWireValue wireValue: String, _ colors: CalStuffColors) -> Color {
        switch wireValue {
        case MealType.breakfast.wireValue: return color(for: .breakfast, colors)
        case MealType.lunch.wireValue: return color(for: .lunch, colors)
        case MealType.dinner.wireValue: return color(for: .dinner, colors)
        default: return color(for: .snacks, colors)
        }
    }
}
