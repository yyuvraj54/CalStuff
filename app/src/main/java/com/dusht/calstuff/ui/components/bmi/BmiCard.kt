package com.dusht.calstuff.ui.components.bmi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.dusht.calstuff.ui.model.BmiConfig
import com.dusht.calstuff.ui.model.Gender
import com.dusht.calstuff.ui.theme.FontSize

@Composable
fun BmiCard(
    config: BmiConfig,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: title + category badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Body Mass Index",
                    color = Color(0xFFBBBBBB),
                    fontSize = FontSize.medium,
                    fontWeight = FontWeight.SemiBold
                )

                // Category badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(config.category.colorHex).copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = config.category.label,
                        color = Color(config.category.colorHex),
                        fontSize = FontSize.xSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gauge
            BmiGauge(
                config = config,
                gaugeSize = 210.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Height", value = "${config.heightCm.toInt()} cm")
                StatDivider()
                StatItem(label = "Weight", value = "${config.weightKg.toInt()} kg")
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color(0xFF222222),
            fontSize = FontSize.medium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color(0xFFBBBBBB),
            fontSize = FontSize.xxSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Color(0xFFEEEEEE))
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 380)
@Composable
private fun PreviewBmiCard() {
    BmiCard(
        config = BmiConfig.mock(),
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 380)
@Composable
private fun PreviewBmiCardOverweight() {
    BmiCard(
        config = BmiConfig(heightCm = 170f, weightKg = 85f, age = 35, gender = Gender.FEMALE),
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F1EB, widthDp = 380)
@Composable
private fun PreviewBmiCardUnderweight() {
    BmiCard(
        config = BmiConfig(heightCm = 180f, weightKg = 55f, age = 19, gender = Gender.MALE),
        modifier = Modifier.padding(16.dp)
    )
}
