package com.dusht.calstuff.ui.model

import java.util.Calendar
import java.util.UUID

data class MealLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mealType: MealType,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val time: String
)

data class DayLog(
    val day: Int,
    val meals: List<MealLogEntry>,
    val calorieGoal: Int
) {
    val totalCalories: Int get() = meals.sumOf { it.calories }
    val totalProtein: Float get() = meals.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs: Float get() = meals.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFat: Float get() = meals.sumOf { it.fat.toDouble() }.toFloat()
    val calorieRatio: Float get() = if (calorieGoal > 0) totalCalories.toFloat() / calorieGoal else 0f
}

data class MonthLogsData(
    val year: Int,
    val month: Int,
    val calorieGoal: Int,
    val dayLogs: Map<Int, DayLog>
) {
    val dayCalorieRatios: Map<Int, Float>
        get() = dayLogs.mapValues { it.value.calorieRatio }

    val highlightedDays: Set<Int>
        get() = dayLogs.keys

    val calendarPercentage: Int
        get() {
            val cal = Calendar.getInstance()
            val today = cal.get(Calendar.DAY_OF_MONTH)
            val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            return if (totalDays > 0) (today * 100) / totalDays else 0
        }

    companion object {
        fun mock(): MonthLogsData {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val today = cal.get(Calendar.DAY_OF_MONTH)
            val calorieGoal = 2200

            val breakfasts = listOf(
                Triple("Oatmeal with Banana", 350, Triple(12f, 58f, 8f)),
                Triple("Scrambled Eggs & Toast", 420, Triple(24f, 35f, 20f)),
                Triple("Greek Yogurt Parfait", 310, Triple(18f, 42f, 9f)),
                Triple("Avocado Toast", 380, Triple(10f, 32f, 22f)),
                Triple("Protein Smoothie", 290, Triple(28f, 35f, 5f)),
                Triple("Pancakes with Syrup", 450, Triple(8f, 72f, 14f))
            )
            val lunches = listOf(
                Triple("Grilled Chicken Salad", 480, Triple(38f, 20f, 26f)),
                Triple("Turkey Sandwich", 520, Triple(32f, 48f, 18f)),
                Triple("Pasta Primavera", 560, Triple(16f, 78f, 18f)),
                Triple("Chicken Tikka Bowl", 610, Triple(42f, 55f, 22f)),
                Triple("Veggie Wrap", 390, Triple(14f, 48f, 16f)),
                Triple("Salmon Poke Bowl", 540, Triple(35f, 52f, 18f))
            )
            val dinners = listOf(
                Triple("Grilled Salmon & Rice", 620, Triple(42f, 52f, 22f)),
                Triple("Chicken Stir Fry", 550, Triple(38f, 45f, 20f)),
                Triple("Dal & Roti", 480, Triple(18f, 62f, 14f)),
                Triple("Steak with Veggies", 680, Triple(48f, 20f, 38f)),
                Triple("Paneer Butter Masala", 570, Triple(22f, 38f, 34f)),
                Triple("Fish Tacos", 510, Triple(30f, 42f, 22f))
            )
            val snacks = listOf(
                Triple("Protein Bar", 210, Triple(20f, 22f, 7f)),
                Triple("Mixed Nuts", 180, Triple(5f, 8f, 16f)),
                Triple("Apple with Peanut Butter", 250, Triple(7f, 30f, 14f)),
                Triple("Trail Mix", 200, Triple(6f, 24f, 10f)),
                Triple("Dark Chocolate", 170, Triple(2f, 20f, 10f))
            )

            val dayLogs = mutableMapOf<Int, DayLog>()
            for (day in 1..today) {
                val seed = day * 7 + month
                val b = breakfasts[seed % breakfasts.size]
                val l = lunches[(seed + 3) % lunches.size]
                val d = dinners[(seed + 5) % dinners.size]
                val s = snacks[(seed + 2) % snacks.size]

                val meals = mutableListOf(
                    MealLogEntry(id = "d${day}_b", name = b.first, mealType = MealType.BREAKFAST, calories = b.second, protein = b.third.first, carbs = b.third.second, fat = b.third.third, time = "8:${15 + (day % 4) * 5} AM"),
                    MealLogEntry(id = "d${day}_l", name = l.first, mealType = MealType.LUNCH, calories = l.second, protein = l.third.first, carbs = l.third.second, fat = l.third.third, time = "12:${30 + (day % 3) * 10} PM"),
                    MealLogEntry(id = "d${day}_d", name = d.first, mealType = MealType.DINNER, calories = d.second, protein = d.third.first, carbs = d.third.second, fat = d.third.third, time = "7:${(day % 4) * 15} PM"),
                )
                if (day % 3 != 0) {
                    meals.add(MealLogEntry(id = "d${day}_s", name = s.first, mealType = MealType.SNACKS, calories = s.second, protein = s.third.first, carbs = s.third.second, fat = s.third.third, time = "4:${15 + (day % 3) * 15} PM"))
                }

                dayLogs[day] = DayLog(day = day, meals = meals, calorieGoal = calorieGoal)
            }

            return MonthLogsData(
                year = year,
                month = month,
                calorieGoal = calorieGoal,
                dayLogs = dayLogs
            )
        }
    }
}
