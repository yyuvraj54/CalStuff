package com.dusht.calstuff.ui.components.widgetgrid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EditModeFab(
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    onAddWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Add widget button — only visible in edit mode
        AnimatedVisibility(
            visible = isEditMode,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            SmallFloatingActionButton(
                onClick = onAddWidget,
                containerColor = Color(0xFFFFD643),
                contentColor = Color(0xFF222222),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add widget"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main FAB: edit or confirm
        FloatingActionButton(
            onClick = onToggleEditMode,
            containerColor = if (isEditMode) Color(0xFF66BB6A) else Color(0xFF222222),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                contentDescription = if (isEditMode) "Confirm layout" else "Edit layout"
            )
        }
    }
}
