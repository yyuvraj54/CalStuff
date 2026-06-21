package com.dusht.calstuff.ui.components.widgetgrid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dusht.calstuff.ui.model.widget.WidgetType
import com.dusht.calstuff.ui.theme.FontSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPickerSheet(
    availableTypes: List<WidgetType>,
    onSelect: (WidgetType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Add Widget",
                fontSize = FontSize.large,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (availableTypes.isEmpty()) {
                Text(
                    text = "All widgets are already on your screen",
                    fontSize = FontSize.medium,
                    color = Color(0xFFBBBBBB),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                availableTypes.forEach { type ->
                    WidgetPickerItem(
                        type = type,
                        onClick = {
                            onSelect(type)
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WidgetPickerItem(
    type: WidgetType,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Size indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFD643).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${type.spanX}x${type.spanY}",
                fontSize = FontSize.xSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD643)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = type.displayName,
                fontSize = FontSize.medium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF222222)
            )
            Text(
                text = "${type.spanX} × ${type.spanY} cells",
                fontSize = FontSize.xSmall,
                color = Color(0xFFBBBBBB)
            )
        }
    }
}
