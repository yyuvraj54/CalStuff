package com.dusht.calstuff.ui.screens.addmeal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import com.dusht.calstuff.ui.model.MealType
import com.dusht.calstuff.ui.theme.FontSize
import java.util.Calendar
import java.util.Locale

data class FoodItem(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val mealType: MealType = MealType.SNACKS
)

/** Validation limits for a single food item */
object MealLimits {
    const val MAX_CALORIES = 5000
    const val MAX_PROTEIN = 300f
    const val MAX_CARBS = 500f
    const val MAX_FAT = 200f
}

fun validateFoodItem(item: FoodItem): List<String> {
    val errors = mutableListOf<String>()
    if (item.name.isBlank()) errors.add("Food name is required")
    if (item.calories <= 0) errors.add("Calories must be greater than 0")
    if (item.calories > MealLimits.MAX_CALORIES) errors.add("Calories cannot exceed ${MealLimits.MAX_CALORIES}")
    if (item.protein > MealLimits.MAX_PROTEIN) errors.add("Protein cannot exceed ${MealLimits.MAX_PROTEIN.toInt()}g")
    if (item.carbs > MealLimits.MAX_CARBS) errors.add("Carbs cannot exceed ${MealLimits.MAX_CARBS.toInt()}g")
    if (item.fat > MealLimits.MAX_FAT) errors.add("Fat cannot exceed ${MealLimits.MAX_FAT.toInt()}g")
    // Macros shouldn't exceed calories (1g protein=4cal, 1g carbs=4cal, 1g fat=9cal)
    val macroCalories = (item.protein * 4 + item.carbs * 4 + item.fat * 9).toInt()
    if (macroCalories > item.calories * 1.5) errors.add("Macros seem too high for the calorie count")
    return errors
}

data class MealEntry(
    val mealType: MealType,
    val foodName: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val servingSize: String,
    val items: List<FoodItem> = emptyList()
)

@Composable
fun AddMealScreen(
    onBack: () -> Unit,
    onSave: (Int, MealEntry) -> Unit = { _, _ -> },
    initialDay: Int = 0,
    editableWindowDays: Int = 1,
    modifier: Modifier = Modifier
) {
    val cal = remember { Calendar.getInstance() }
    val today = remember { cal.get(Calendar.DAY_OF_MONTH) }
    val resolvedInitialDay = if (initialDay == 0) today else initialDay
    var selectedDay by remember { mutableStateOf(resolvedInitialDay) }

    var aiDescription by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    val foodItems = remember { mutableStateListOf<FoodItem>() }
    var showAddManualForm by remember { mutableStateOf(false) }

    // Manual form fields
    var manualName by remember { mutableStateOf("") }
    var manualCal by remember { mutableStateOf("") }
    var manualP by remember { mutableStateOf("") }
    var manualC by remember { mutableStateOf("") }
    var manualF by remember { mutableStateOf("") }
    var manualMealType by remember { mutableStateOf(MealType.SNACKS) }

    val canSave = foodItems.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F1EB))
            .statusBarsPadding()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 80.dp) // space for sticky save
        ) {
            // ── Top bar ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFF222222))
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Add Meal", fontSize = FontSize.xxLarge, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                    val dayLabel = when (selectedDay) {
                        today -> "Today"
                        today - 1 -> "Yesterday"
                        else -> {
                            val monthName = java.text.DateFormatSymbols(Locale.getDefault()).months[cal.get(Calendar.MONTH)]
                            "$monthName $selectedDay"
                        }
                    }
                    Text(dayLabel, fontSize = FontSize.small, fontWeight = FontWeight.Medium, color = Color(0xFFBBBBBB))
                }
            }

            // ── Date selector chips ──
            if (editableWindowDays > 1) {
                Spacer(Modifier.height(12.dp))
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
                        val isSelected = selectedDay == day
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF222222) else Color.White)
                                .then(if (!isSelected) Modifier.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(20.dp)) else Modifier)
                                .clickable { selectedDay = day }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = chipLabel,
                                fontSize = FontSize.small,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF666666)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── AI Description ──
            Text("Describe what you ate", fontSize = FontSize.medium, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222))
            Text("AI will estimate calories & macros for each item", fontSize = FontSize.xSmall, color = Color(0xFFBBBBBB))
            Spacer(Modifier.height(10.dp))

            val wordCount = aiDescription.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
            OutlinedTextField(
                value = aiDescription,
                onValueChange = { v ->
                    val wc = v.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
                    if (wc <= 300 || v.length < aiDescription.length) aiDescription = v
                },
                placeholder = { Text("e.g. 2 roti with dal, a bowl of rice, and buttermilk", color = Color(0xFFCCCCCC), fontSize = FontSize.small) },
                supportingText = { Text("$wordCount / 300 words", fontSize = FontSize.xxSmall, color = if (wordCount > 280) Color(0xFFF85B4E) else Color(0xFFBBBBBB)) },
                modifier = Modifier.fillMaxWidth().height(130.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD643), unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, cursorColor = Color(0xFF222222)
                )
            )
            Spacer(Modifier.height(16.dp))

            // ── Food items list ──
            if (foodItems.isNotEmpty()) {
                Text("${foodItems.size} item${if (foodItems.size > 1) "s" else ""} added", fontSize = FontSize.medium, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222))
                Spacer(Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        foodItems.forEachIndexed { index, item ->
                            FoodItemRow(
                                item = item,
                                onEdit = { edited -> foodItems[index] = edited },
                                onRemove = { foodItems.removeAt(index) }
                            )
                            if (index < foodItems.lastIndex) {
                                HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F0F0))
                            }
                        }

                        // Totals
                        HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color(0xFFE0E0E0), thickness = 1.5.dp)
                        val tCal = foodItems.sumOf { it.calories }
                        val tP = foodItems.sumOf { it.protein.toDouble() }.toFloat()
                        val tC = foodItems.sumOf { it.carbs.toDouble() }.toFloat()
                        val tF = foodItems.sumOf { it.fat.toDouble() }.toFloat()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontSize = FontSize.medium, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                            Text("$tCal kcal", fontSize = FontSize.medium, fontWeight = FontWeight.Bold, color = Color(0xFFF85B4E))
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MacroBadge("P", "${tP.toInt()}g", Color(0xFFFFD643))
                            MacroBadge("C", "${tC.toInt()}g", Color(0xFFF85B4E))
                            MacroBadge("F", "${tF.toInt()}g", Color(0xFF222222))
                        }

                        // Add more button inside the card
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAddManualForm = true },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFFFFD643), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add another item", fontSize = FontSize.small, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD643))
                        }
                    }
                }
            } else {
                // Empty state — show add button in center
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD643))
                                .clickable { showAddManualForm = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, "Add item", tint = Color(0xFF222222), modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Add item manually", fontSize = FontSize.small, color = Color(0xFFBBBBBB))
                    }
                }
            }

            // ── Manual add form ──
            AnimatedVisibility(visible = showAddManualForm, enter = fadeIn() + expandVertically()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Item", fontSize = FontSize.medium, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222))
                        Spacer(Modifier.height(10.dp))

                        // Meal type pills
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MealType.entries.forEach { type ->
                                MealTypePill(type, manualMealType == type, { manualMealType = type }, Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        MealTextField(manualName, { manualName = it }, "Food name", "e.g. Grilled Chicken")
                        Spacer(Modifier.height(8.dp))
                        MealTextField(manualCal, { manualCal = it.filter { c -> c.isDigit() } }, "Calories (kcal)", "e.g. 450", KeyboardType.Number)
                        Spacer(Modifier.height(8.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MealTextField(manualP, { manualP = it.filter { c -> c.isDigit() || c == '.' } }, "Protein", "0", KeyboardType.Decimal, Modifier.weight(1f))
                            MealTextField(manualC, { manualC = it.filter { c -> c.isDigit() || c == '.' } }, "Carbs", "0", KeyboardType.Decimal, Modifier.weight(1f))
                            MealTextField(manualF, { manualF = it.filter { c -> c.isDigit() || c == '.' } }, "Fat", "0", KeyboardType.Decimal, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))

                        var manualError by remember { mutableStateOf("") }

                        if (manualError.isNotBlank()) {
                            Text(manualError, fontSize = FontSize.xSmall, color = Color(0xFFF85B4E))
                            Spacer(Modifier.height(6.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showAddManualForm = false; manualError = "" },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel", fontSize = FontSize.small, color = Color(0xFF888888)) }

                            Button(
                                onClick = {
                                    val candidate = FoodItem(
                                        name = manualName.trim(),
                                        calories = manualCal.toIntOrNull() ?: 0,
                                        protein = manualP.toFloatOrNull() ?: 0f,
                                        carbs = manualC.toFloatOrNull() ?: 0f,
                                        fat = manualF.toFloatOrNull() ?: 0f,
                                        mealType = manualMealType
                                    )
                                    val errors = validateFoodItem(candidate)
                                    if (errors.isEmpty()) {
                                        foodItems.add(candidate)
                                        manualName = ""; manualCal = ""; manualP = ""; manualC = ""; manualF = ""
                                        manualMealType = MealType.SNACKS
                                        manualError = ""
                                        showAddManualForm = false
                                    } else {
                                        manualError = errors.first()
                                    }
                                },
                                enabled = manualName.isNotBlank() && manualCal.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD643), disabledContainerColor = Color(0xFFE8E8E8)),
                                modifier = Modifier.weight(1f)
                            ) { Text("Add", fontSize = FontSize.small, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222)) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }

        // ── Sticky bottom buttons ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFFF3F1EB))
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Analyze with AI button
            if (aiDescription.isNotBlank()) {
                Button(
                    onClick = {
                        isAnalyzing = true
                        val mockItems = parseMockItems(aiDescription)
                        foodItems.addAll(mockItems)
                        isAnalyzing = false
                        aiDescription = ""
                    },
                    enabled = !isAnalyzing,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD643), contentColor = Color(0xFF222222))
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF222222))
                        Spacer(Modifier.width(8.dp))
                        Text("Analyzing...", fontSize = FontSize.small, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Default.Search, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze with AI", fontSize = FontSize.small, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Save button
            Button(
                onClick = {
                    // Validate all items before saving
                    val allErrors = foodItems.mapIndexedNotNull { i, item ->
                        val errs = validateFoodItem(item)
                        if (errs.isNotEmpty()) "${item.name}: ${errs.first()}" else null
                    }
                    if (allErrors.isNotEmpty()) {
                        // TODO: show validation errors (for now items are validated on add)
                        return@Button
                    }

                    val tCal = foodItems.sumOf { it.calories }
                    val tP = foodItems.sumOf { it.protein.toDouble() }.toFloat()
                    val tC = foodItems.sumOf { it.carbs.toDouble() }.toFloat()
                    val tF = foodItems.sumOf { it.fat.toDouble() }.toFloat()
                    onSave(
                        selectedDay,
                        MealEntry(
                            mealType = foodItems.firstOrNull()?.mealType ?: MealType.SNACKS,
                            foodName = foodItems.firstOrNull()?.name ?: "",
                            calories = tCal,
                            protein = tP,
                            carbs = tC,
                            fat = tF,
                            servingSize = "",
                            items = foodItems.toList()
                        )
                    )
                    onBack()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222), disabledContainerColor = Color(0xFFDDDDDD))
            ) {
                Text(
                    if (canSave) "Save ${foodItems.size} Item${if (foodItems.size > 1) "s" else ""}" else "Add items to save",
                    fontSize = FontSize.body, fontWeight = FontWeight.SemiBold,
                    color = if (canSave) Color.White else Color(0xFF999999)
                )
            }
        }
    }
}

// ─── Food Item Row (display + edit) ──────────────

@Composable
private fun FoodItemRow(item: FoodItem, onEdit: (FoodItem) -> Unit, onRemove: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(item) { mutableStateOf(item.name) }
    var editCal by remember(item) { mutableStateOf(item.calories.toString()) }
    var editP by remember(item) { mutableStateOf(item.protein.toInt().toString()) }
    var editC by remember(item) { mutableStateOf(item.carbs.toInt().toString()) }
    var editF by remember(item) { mutableStateOf(item.fat.toInt().toString()) }
    var editMealType by remember(item) { mutableStateOf(item.mealType) }

    Column {
        if (isEditing) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MealType.entries.forEach { type -> MealTypePill(type, editMealType == type, { editMealType = type }, Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(editName, { editName = it }, label = { Text("Name", fontSize = FontSize.xxSmall) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD643), unfocusedBorderColor = Color(0xFFE8E8E8)))
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("kcal" to editCal, "P(g)" to editP, "C(g)" to editC, "F(g)" to editF).forEachIndexed { i, (lbl, v) ->
                    OutlinedTextField(v, { nv ->
                        val filtered = nv.filter { c -> c.isDigit() }
                        when (i) { 0 -> editCal = filtered; 1 -> editP = filtered; 2 -> editC = filtered; 3 -> editF = filtered }
                    }, label = { Text(lbl, fontSize = FontSize.xxSmall) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD643), unfocusedBorderColor = Color(0xFFE8E8E8)))
                }
            }
            var editError by remember { mutableStateOf("") }

            if (editError.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(editError, fontSize = FontSize.xSmall, color = Color(0xFFF85B4E))
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton({ isEditing = false; editError = "" }, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) { Text("Cancel", fontSize = FontSize.xSmall, color = Color(0xFF888888)) }
                Button(
                    onClick = {
                        val candidate = FoodItem(editName.trim().ifBlank { item.name }, editCal.toIntOrNull() ?: item.calories, editP.toFloatOrNull() ?: item.protein, editC.toFloatOrNull() ?: item.carbs, editF.toFloatOrNull() ?: item.fat, editMealType)
                        val errors = validateFoodItem(candidate)
                        if (errors.isEmpty()) { onEdit(candidate); isEditing = false; editError = "" }
                        else { editError = errors.first() }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD643)),
                    modifier = Modifier.weight(1f)
                ) { Text("Save", fontSize = FontSize.xSmall, color = Color(0xFF222222)) }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.name, fontSize = FontSize.medium, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222))
                    val tc = Color(item.mealType.color)
                    Box(Modifier.padding(top = 3.dp).clip(RoundedCornerShape(8.dp)).background(tc.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(item.mealType.label, fontSize = FontSize.xxxSmall, fontWeight = FontWeight.SemiBold, color = tc)
                    }
                }
                Text("${item.calories} kcal", fontSize = FontSize.medium, fontWeight = FontWeight.Bold, color = Color(0xFF888888))
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MacroBadge("P", "${item.protein.toInt()}g", Color(0xFFFFD643))
                MacroBadge("C", "${item.carbs.toInt()}g", Color(0xFFF85B4E))
                MacroBadge("F", "${item.fat.toInt()}g", Color(0xFF222222))
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Edit", fontSize = FontSize.xSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD643), modifier = Modifier.clickable { isEditing = true })
                Text("Remove", fontSize = FontSize.xSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFFF85B4E), modifier = Modifier.clickable { onRemove() })
            }
        }
    }
}

// ─── Shared Components ───────────────────────────

@Composable
private fun MacroBadge(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(4.dp))
        Text("$label: $value", fontSize = FontSize.xxSmall, color = Color(0xFF888888))
    }
}

@Composable
private fun MealTextField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = FontSize.small) },
        placeholder = { Text(placeholder, color = Color(0xFFCCCCCC), fontSize = FontSize.small) },
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFD643), unfocusedBorderColor = Color(0xFFE0E0E0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, cursorColor = Color(0xFF222222))
    )
}

@Composable
private fun MealTypePill(type: MealType, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val c = Color(type.color)
    Box(
        modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) c else Color.Transparent)
            .then(if (!isSelected) Modifier.border(1.5.dp, c, RoundedCornerShape(20.dp)) else Modifier)
            .clickable(onClick = onSelect).padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(type.label, fontSize = FontSize.xxxSmall, fontWeight = FontWeight.SemiBold, color = if (isSelected) { if (type == MealType.DINNER) Color.White else Color.Black } else c, maxLines = 1)
    }
}

private fun parseMockItems(description: String): List<FoodItem> {
    if (description.isBlank()) return emptyList()
    return description.replace(" and ", ",").replace("&", ",").split(",").map { it.trim() }.filter { it.isNotBlank() }.map { name ->
        val wc = name.split(" ").size
        val lower = name.lowercase()
        val mt = when {
            lower.contains("egg") || lower.contains("toast") || lower.contains("cereal") || lower.contains("oatmeal") -> MealType.BREAKFAST
            lower.contains("rice") || lower.contains("roti") || lower.contains("dal") || lower.contains("curry") || lower.contains("salad") -> MealType.LUNCH
            lower.contains("soup") || lower.contains("steak") || lower.contains("pasta") -> MealType.DINNER
            else -> MealType.SNACKS
        }
        FoodItem(name.replaceFirstChar { it.uppercase() }, 150 + wc * 50, (8 + wc * 3).toFloat(), (20 + wc * 8).toFloat(), (5 + wc * 2).toFloat(), mt)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB)
@Composable
private fun PreviewAddMealScreen() { AddMealScreen(onBack = {}) }
