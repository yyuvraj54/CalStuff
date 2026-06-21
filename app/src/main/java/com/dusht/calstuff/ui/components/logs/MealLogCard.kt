package com.dusht.calstuff.ui.components.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dusht.calstuff.ui.model.MealLogEntry
import com.dusht.calstuff.ui.model.MealType
import com.dusht.calstuff.ui.theme.FontSize

@Composable
fun MealLogCard(
    entry: MealLogEntry,
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
            // Top row: meal type dot + label + time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(entry.mealType.color))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.mealType.label,
                        color = Color(0xFF999999),
                        fontSize = FontSize.xSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = entry.time,
                    color = Color(0xFFBBBBBB),
                    fontSize = FontSize.xSmall,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Food name
            Text(
                text = entry.name,
                color = Color.Black,
                fontSize = FontSize.medium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Calories + macros row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entry.calories} kcal",
                    color = Color.Black,
                    fontSize = FontSize.smallMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroPill(label = "P", value = "${entry.protein.toInt()}g", color = Color(0xFFFFD643))
                    MacroPill(label = "C", value = "${entry.carbs.toInt()}g", color = Color(0xFFF85B4E))
                    MacroPill(label = "F", value = "${entry.fat.toInt()}g", color = Color(0xFF222222))
                }
            }
        }
    }
}

@Composable
private fun MacroPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label ",
            color = color,
            fontSize = FontSize.xxxSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color.Black.copy(alpha = 0.7f),
            fontSize = FontSize.xxxSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB)
@Composable
private fun PreviewMealLogCard() {
    MealLogCard(
        entry = MealLogEntry(
            name = "Grilled Chicken Salad",
            mealType = MealType.LUNCH,
            calories = 480,
            protein = 38f,
            carbs = 20f,
            fat = 26f,
            time = "12:30 PM"
        ),
        modifier = Modifier.padding(16.dp)
    )
}
