import Foundation

/// UI-layer models mirroring Android's `ui/model/{BmiConfig,HomeScreenData}.kt` — deliberately
/// plain Swift (not shared/`commonMain`), since these are presentation-only shapes derived from
/// domain data, same as Android keeps them in the `:app` module rather than `:shared`.

enum BmiGender {
    case male, female, other
}

enum BmiCategory: String {
    case underweight = "Underweight"
    case normal = "Normal"
    case overweight = "Overweight"
    case obese = "Obese"

    static func from(bmi: Float, age: Int, gender: BmiGender) -> BmiCategory {
        if age < 20 {
            switch bmi {
            case ..<17: return .underweight
            case ..<24: return .normal
            case ..<29: return .overweight
            default: return .obese
            }
        }
        if age >= 65 {
            switch bmi {
            case ..<20: return .underweight
            case ..<27: return .normal
            case ..<32: return .overweight
            default: return .obese
            }
        }
        if gender == .female {
            switch bmi {
            case ..<18.5: return .underweight
            case ..<24: return .normal
            case ..<29: return .overweight
            default: return .obese
            }
        }
        switch bmi {
        case ..<18.5: return .underweight
        case ..<25: return .normal
        case ..<30: return .overweight
        default: return .obese
        }
    }

    func meaning(gender: BmiGender) -> String {
        switch self {
        case .underweight: return "You may need to gain some weight"
        case .normal: return "You're at a healthy weight"
        case .overweight: return gender == .female ? "Slightly above ideal range for women" : "Consider a balanced diet & exercise"
        case .obese: return "Consult a healthcare professional"
        }
    }
}

struct BmiConfig {
    let heightCm: Float
    let weightKg: Float
    let age: Int
    let gender: BmiGender

    var bmiValue: Float {
        guard heightCm > 0 else { return 0 }
        let heightM = heightCm / 100
        return weightKg / (heightM * heightM)
    }

    var category: BmiCategory {
        BmiCategory.from(bmi: bmiValue, age: age, gender: gender)
    }

    var healthNote: String {
        if age < 20 { return "BMI interpretation varies for teens. Consult a pediatrician." }
        if age >= 65 { return "For seniors, BMI 25–27 can be healthy. Discuss with your doctor." }
        return category.meaning(gender: gender)
    }
}

struct DayProgress {
    let dayName: String
    let date: String
    let caloriesConsumed: Int
}

/// Fixed macro goals shown on the daily-progress ring — hardcoded on Android too (not derived
/// from the profile's calorie goal), matched here deliberately rather than "fixed".
enum MacroGoals {
    static let protein: Float = 150
    static let carbs: Float = 250
    static let fat: Float = 70
}
