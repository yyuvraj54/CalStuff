package com.dusht.calstuff.ui.model

enum class Gender { MALE, FEMALE, OTHER }

data class BmiConfig(
    val heightCm: Float,
    val weightKg: Float,
    val age: Int,
    val gender: Gender
) {
    /** Standard BMI = weight(kg) / height(m)² — same formula for all. */
    val bmiValue: Float
        get() {
            if (heightCm <= 0f) return 0f
            val heightM = heightCm / 100f
            return weightKg / (heightM * heightM)
        }

    /**
     * Category adjusted for age and gender:
     * - Children/teens (<20): shifted thresholds (growing bodies)
     * - Elderly (65+): slightly higher healthy range
     * - Women: slightly lower overweight threshold (different body composition)
     */
    val category: BmiCategory
        get() = BmiCategory.from(bmiValue, age, gender)

    /** Health context based on age and gender. */
    val healthNote: String
        get() = when {
            age < 20 -> "BMI interpretation varies for teens. Consult a pediatrician."
            age >= 65 -> "For seniors, BMI 25–27 can be healthy. Discuss with your doctor."
            else -> category.meaning(gender)
        }

    companion object {
        fun mock() = BmiConfig(
            heightCm = 175f,
            weightKg = 72f,
            age = 25,
            gender = Gender.MALE
        )
    }
}

enum class BmiCategory(val label: String, val colorHex: Long) {
    UNDERWEIGHT("Underweight", 0xFF42A5F5),
    NORMAL("Normal", 0xFF66BB6A),
    OVERWEIGHT("Overweight", 0xFFFFD643),
    OBESE("Obese", 0xFFF85B4E);

    fun meaning(gender: Gender): String = when (this) {
        UNDERWEIGHT -> "You may need to gain some weight"
        NORMAL -> "You're at a healthy weight"
        OVERWEIGHT -> when (gender) {
            Gender.FEMALE -> "Slightly above ideal range for women"
            else -> "Consider a balanced diet & exercise"
        }
        OBESE -> "Consult a healthcare professional"
    }

    companion object {
        fun from(bmi: Float, age: Int, gender: Gender): BmiCategory {
            // Children/teens: use adjusted thresholds
            if (age < 20) {
                return when {
                    bmi < 17f -> UNDERWEIGHT
                    bmi < 24f -> NORMAL
                    bmi < 29f -> OVERWEIGHT
                    else -> OBESE
                }
            }

            // Elderly: slightly higher healthy range
            if (age >= 65) {
                return when {
                    bmi < 20f -> UNDERWEIGHT
                    bmi < 27f -> NORMAL
                    bmi < 32f -> OVERWEIGHT
                    else -> OBESE
                }
            }

            // Adult women: slightly tighter overweight threshold
            if (gender == Gender.FEMALE) {
                return when {
                    bmi < 18.5f -> UNDERWEIGHT
                    bmi < 24f -> NORMAL
                    bmi < 29f -> OVERWEIGHT
                    else -> OBESE
                }
            }

            // Adult men / other: standard WHO thresholds
            return when {
                bmi < 18.5f -> UNDERWEIGHT
                bmi < 25f -> NORMAL
                bmi < 30f -> OVERWEIGHT
                else -> OBESE
            }
        }
    }
}
