package com.dusht.calstuff.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dusht.calstuff.ui.model.BmiConfig
import com.dusht.calstuff.ui.model.DayLog
import com.dusht.calstuff.ui.model.Gender
import com.dusht.calstuff.ui.model.MealLogEntry
import com.dusht.calstuff.ui.model.MealType
import com.dusht.calstuff.ui.model.MonthLogsData
import com.dusht.shared.model.DayNutrition
import com.dusht.shared.model.MealEntry
import com.dusht.shared.model.UserProfile
import com.dusht.shared.repository.NutritionRepository
import com.dusht.shared.repository.StreakRepository
import com.dusht.shared.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val streakRepository: StreakRepository,
) : ViewModel() {

    private val cal = Calendar.getInstance()
    private val currentYear = cal.get(Calendar.YEAR)
    private val currentMonth = cal.get(Calendar.MONTH) + 1  // 1-based

    private val today: Int get() = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    // Empty state on start; Room populates it immediately, Firestore syncs in background.
    private val _state = MutableStateFlow(NutritionUiState.empty())
    val state: StateFlow<NutritionUiState> = _state.asStateFlow()

    init {
        observeFirestoreData()
    }

    private fun observeFirestoreData() {
        viewModelScope.launch {
            combine(
                nutritionRepository.observeMonthLogs(currentYear, currentMonth),
                userProfileRepository.observeProfile(),
                streakRepository.observeStreak(),
            ) { dayNutritionMap, profile, streak ->
                val goalFromProfile = profile?.dailyCalorieGoal ?: _state.value.dailyCalorieGoal
                NutritionUiState(
                    dailyCalorieGoal = goalFromProfile,
                    monthLogsData = MonthLogsData(
                        year = currentYear,
                        month = currentMonth,
                        calorieGoal = goalFromProfile,
                        dayLogs = dayNutritionMap.mapValues { (_, v) -> v.toDayLog() },
                    ),
                    bmiConfig = profile?.toBmiConfig() ?: _state.value.bmiConfig,
                    streakDays = streak.currentStreak,
                    editableWindowDays = _state.value.editableWindowDays,
                )
            }
                .catch { /* Firestore error: keep existing state to avoid blank screen */ }
                .collect { _state.value = it }
        }
    }

    fun canEditDay(day: Int): Boolean {
        val windowDays = _state.value.editableWindowDays
        return day in 1..today && (today - day) < windowDays
    }

    fun addMealForDay(day: Int, entry: MealLogEntry) {
        if (!canEditDay(day)) return
        // Optimistic update for instant UI feedback.
        _state.update { current ->
            val dayLogs = current.monthLogsData.dayLogs.toMutableMap()
            val dayLog = dayLogs[day] ?: DayLog(day, emptyList(), current.dailyCalorieGoal)
            dayLogs[day] = dayLog.copy(meals = dayLog.meals + entry)
            current.copy(monthLogsData = current.monthLogsData.copy(dayLogs = dayLogs))
        }
        // Persist to Firestore (Firestore snapshot listener will confirm/correct).
        viewModelScope.launch {
            nutritionRepository.addMeal(currentYear, currentMonth, day, entry.toMealEntry())
        }
        updateStreakForToday(day)
    }

    fun updateMealForDay(day: Int, mealId: String, updated: MealLogEntry) {
        if (!canEditDay(day)) return
        _state.update { current ->
            val dayLogs = current.monthLogsData.dayLogs.toMutableMap()
            val dayLog = dayLogs[day] ?: return@update current
            dayLogs[day] = dayLog.copy(meals = dayLog.meals.map { if (it.id == mealId) updated else it })
            current.copy(monthLogsData = current.monthLogsData.copy(dayLogs = dayLogs))
        }
        viewModelScope.launch {
            nutritionRepository.updateMeal(currentYear, currentMonth, day, updated.toMealEntry())
        }
    }

    fun deleteMealForDay(day: Int, mealId: String) {
        if (!canEditDay(day)) return
        _state.update { current ->
            val dayLogs = current.monthLogsData.dayLogs.toMutableMap()
            val dayLog = dayLogs[day] ?: return@update current
            dayLogs[day] = dayLog.copy(meals = dayLog.meals.filter { it.id != mealId })
            current.copy(monthLogsData = current.monthLogsData.copy(dayLogs = dayLogs))
        }
        viewModelScope.launch {
            nutritionRepository.deleteMeal(currentYear, currentMonth, day, mealId)
        }
    }

    /** Called when a meal is added for today so streak logic runs in Firestore. */
    private fun updateStreakForToday(day: Int) {
        if (day != today) return
        val todayStr = "%04d-%02d-%02d".format(currentYear, currentMonth, today)
        viewModelScope.launch {
            streakRepository.updateStreak(todayStr)
        }
    }

    // ── Domain → UI model mappers (private, keep UI model details out of domain layer) ──

    private fun DayNutrition.toDayLog() = DayLog(
        day = day,
        calorieGoal = calorieGoal,
        meals = meals.map { it.toMealLogEntry() },
    )

    private fun MealEntry.toMealLogEntry() = MealLogEntry(
        id = id,
        name = name,
        mealType = MealType.entries.firstOrNull { it.name == mealType } ?: MealType.SNACKS,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        time = time,
    )

    private fun MealLogEntry.toMealEntry() = MealEntry(
        id = id,
        name = name,
        mealType = mealType.name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        time = time,
    )

    private fun UserProfile.toBmiConfig() = BmiConfig(
        heightCm = heightCm,
        weightKg = weightKg,
        age = age,
        gender = when (gender) {
            "MALE" -> Gender.MALE
            "FEMALE" -> Gender.FEMALE
            else -> Gender.OTHER
        },
    )
}
