package com.dusht.calstuff.ui.screens.navscreen.meals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.ui.model.MealLogEntry
import com.dusht.calstuff.ui.model.MealType
import com.dusht.calstuff.ui.theme.FontSize
import com.dusht.calstuff.vm.NutritionViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun MealsScreen(
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cal = remember { Calendar.getInstance() }
    val today = remember { cal.get(Calendar.DAY_OF_MONTH) }
    val editableWindowDays = state.editableWindowDays

    var selectedDay by remember { mutableIntStateOf(today) }
    val isEditable = viewModel.canEditDay(selectedDay)
    val dayLog = state.monthLogsData.dayLogs[selectedDay]
    val meals = dayLog?.meals ?: emptyList()

    var editingMealId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        val headerLabel = when (selectedDay) {
            today -> "Today's Meals"
            today - 1 -> "Yesterday's Meals"
            else -> {
                val monthName = java.text.DateFormatSymbols(Locale.getDefault()).months[cal.get(Calendar.MONTH)]
                "$monthName $selectedDay"
            }
        }
        Text(
            text = headerLabel,
            color = Color.Black,
            fontSize = FontSize.xLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Summary
        if (dayLog != null) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.Black, fontWeight = FontWeight.SemiBold)) {
                        append("${dayLog.totalCalories}")
                    }
                    withStyle(SpanStyle(color = Color(0xFF999999))) {
                        append(" / ${state.dailyCalorieGoal} kcal")
                    }
                },
                fontSize = FontSize.medium
            )
        }

        // Date selector chips
        if (editableWindowDays > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (offset in 0 until editableWindowDays) {
                    val day = today - offset
                    if (day < 1) break
                    val chipLabel = when (offset) {
                        0 -> "Today"
                        1 -> "Yesterday"
                        else -> {
                            val monthName = java.text.DateFormatSymbols(Locale.getDefault()).months[cal.get(Calendar.MONTH)]
                            "$monthName $day"
                        }
                    }
                    val isChipSelected = selectedDay == day
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isChipSelected) Color(0xFF222222) else Color.White)
                            .then(if (!isChipSelected) Modifier.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(20.dp)) else Modifier)
                            .clickable {
                                selectedDay = day
                                editingMealId = null
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = chipLabel,
                            fontSize = FontSize.small,
                            fontWeight = FontWeight.Medium,
                            color = if (isChipSelected) Color.White else Color(0xFF666666)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (meals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEditable) "No meals logged" else "No meals logged for this day",
                        color = Color(0xFFBBBBBB),
                        fontSize = FontSize.medium,
                        fontWeight = FontWeight.Medium
                    )
                    if (isEditable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first meal",
                            color = Color(0xFFDDDDDD),
                            fontSize = FontSize.small
                        )
                    }
                }
            }
        } else {
            // Meal cards grouped by type
            val grouped = meals.groupBy { it.mealType }
            val orderedTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACKS)

            orderedTypes.forEach { mealType ->
                val typeMeals = grouped[mealType] ?: return@forEach

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(mealType.color))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mealType.label,
                        color = Color(0xFF666666),
                        fontSize = FontSize.smallMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                typeMeals.forEach { meal ->
                    val isEditing = editingMealId == meal.id
                    EditableMealCard(
                        meal = meal,
                        isEditing = isEditing,
                        showActions = isEditable,
                        onEditClick = {
                            editingMealId = if (isEditing) null else meal.id
                        },
                        onDeleteClick = {
                            viewModel.deleteMealForDay(selectedDay, meal.id)
                            if (editingMealId == meal.id) editingMealId = null
                        },
                        onSaveEdit = { updated ->
                            viewModel.updateMealForDay(selectedDay, meal.id, updated)
                            editingMealId = null
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun EditableMealCard(
    meal: MealLogEntry,
    isEditing: Boolean,
    showActions: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveEdit: (MealLogEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.name,
                        color = Color.Black,
                        fontSize = FontSize.medium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = meal.time,
                        color = Color(0xFFBBBBBB),
                        fontSize = FontSize.xSmall
                    )
                }
                if (showActions) {
                    Row {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = if (isEditing) Color(0xFFFFD643) else Color(0xFFBBBBBB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFF85B4E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${meal.calories} kcal",
                    color = Color.Black,
                    fontSize = FontSize.smallMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MacroPill("P", "${meal.protein.toInt()}g", Color(0xFFFFD643))
                    MacroPill("C", "${meal.carbs.toInt()}g", Color(0xFFF85B4E))
                    MacroPill("F", "${meal.fat.toInt()}g", Color(0xFF222222))
                }
            }

            AnimatedVisibility(
                visible = isEditing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                InlineEditForm(meal = meal, onSave = onSaveEdit)
            }
        }
    }
}

@Composable
private fun InlineEditForm(
    meal: MealLogEntry,
    onSave: (MealLogEntry) -> Unit
) {
    var name by remember(meal.id) { mutableStateOf(meal.name) }
    var calories by remember(meal.id) { mutableStateOf(meal.calories.toString()) }
    var protein by remember(meal.id) { mutableStateOf(meal.protein.toInt().toString()) }
    var carbs by remember(meal.id) { mutableStateOf(meal.carbs.toInt().toString()) }
    var fat by remember(meal.id) { mutableStateOf(meal.fat.toInt().toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        EditField("Name", name) { name = it }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EditField("Cal", calories, KeyboardType.Number, Modifier.weight(1f)) { calories = it }
            EditField("Protein", protein, KeyboardType.Number, Modifier.weight(1f)) { protein = it }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EditField("Carbs", carbs, KeyboardType.Number, Modifier.weight(1f)) { carbs = it }
            EditField("Fat", fat, KeyboardType.Number, Modifier.weight(1f)) { fat = it }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                onSave(
                    meal.copy(
                        name = name,
                        calories = calories.toIntOrNull() ?: meal.calories,
                        protein = protein.toFloatOrNull() ?: meal.protein,
                        carbs = carbs.toFloatOrNull() ?: meal.carbs,
                        fat = fat.toFloatOrNull() ?: meal.fat
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD643))
        ) {
            Text("Save", color = Color.Black, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = FontSize.xxxSmall) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFD643),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            cursorColor = Color(0xFFFFD643)
        )
    )
}

@Composable
private fun MacroPill(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$label ", color = color, fontSize = FontSize.xxxSmall, fontWeight = FontWeight.Bold)
        Text(value, color = Color.Black.copy(alpha = 0.7f), fontSize = FontSize.xxxSmall, fontWeight = FontWeight.Medium)
    }
}
