package com.dusht.calstuff.ui.components.widgetgrid

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dusht.calstuff.ui.model.HomeScreenData
import com.dusht.calstuff.ui.model.widget.GridLayout
import com.dusht.calstuff.ui.model.widget.GridPacker
import kotlin.math.roundToInt

private val GRID_GAP = 12.dp

@Composable
fun WidgetGrid(
    layout: GridLayout,
    data: HomeScreenData,
    isEditMode: Boolean,
    onLayoutChanged: (List<com.dusht.calstuff.ui.model.widget.WidgetPlacement>) -> Unit = {},
    onDragStateChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalGap = GRID_GAP * (layout.columns - 1)
        val cellSize = (maxWidth - totalGap) / layout.columns
        val density = LocalDensity.current
        val cellSizePx = with(density) { cellSize.toPx() }
        val gapPx = with(density) { GRID_GAP.toPx() }
        val stepPx = cellSizePx + gapPx

        // Drag state
        var draggingId by remember { mutableStateOf<String?>(null) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }

        // The last grid cell the dragged widget was hovering over — only repack when this changes
        var lastHoverCol by remember { mutableIntStateOf(-1) }
        var lastHoverRow by remember { mutableIntStateOf(-1) }

        // The "others" layout — positions of non-dragged widgets, repacked around the dragged one
        var othersPacked by remember(layout.placements) {
            mutableStateOf(layout.placements)
        }

        // The dragged widget's committed grid position (where it will land)
        var draggedTargetCol by remember { mutableIntStateOf(0) }
        var draggedTargetRow by remember { mutableIntStateOf(0) }

        // Build the display list: others from packed positions + dragged at its target
        val displayPlacements = if (draggingId != null) {
            val dragged = layout.placements.find { it.id == draggingId }
            if (dragged != null) {
                val draggedDisplay = dragged.copy(col = draggedTargetCol, row = draggedTargetRow)
                othersPacked.filter { it.id != draggingId } + draggedDisplay
            } else othersPacked
        } else {
            othersPacked
        }

        val maxRowEnd = if (displayPlacements.isEmpty()) 0
        else displayPlacements.maxOf { it.row + it.spanY }
        val totalHeight = cellSize * maxRowEnd + GRID_GAP * (maxRowEnd - 1).coerceAtLeast(0)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
        ) {
            displayPlacements.forEachIndexed { index, placement ->
                val widgetWidth = cellSize * placement.spanX + GRID_GAP * (placement.spanX - 1)
                val widgetHeight = cellSize * placement.spanY + GRID_GAP * (placement.spanY - 1)

                val gridXPx = with(density) {
                    (cellSize * placement.col + GRID_GAP * placement.col).roundToPx()
                }
                val gridYPx = with(density) {
                    (cellSize * placement.row + GRID_GAP * placement.row).roundToPx()
                }

                val isDragging = draggingId == placement.id

                // Others smoothly animate to new positions
                val animatedOffset by animateIntOffsetAsState(
                    targetValue = IntOffset(gridXPx, gridYPx),
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "pos_${placement.id}"
                )

                val dragModifier = if (isEditMode) {
                    Modifier.pointerInput(placement.id) {
                        detectDragGestures(
                            onDragStart = {
                                draggingId = placement.id
                                dragOffset = Offset.Zero
                                draggedTargetCol = placement.col
                                draggedTargetRow = placement.row
                                lastHoverCol = placement.col
                                lastHoverRow = placement.row
                                onDragStateChanged(true)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset = Offset(
                                    dragOffset.x + dragAmount.x,
                                    dragOffset.y + dragAmount.y
                                )

                                // What grid cell is the center of the dragged widget over?
                                val centerX = gridXPx + dragOffset.x + (widgetWidth.value * density.density) / 2f
                                val centerY = gridYPx + dragOffset.y + (widgetHeight.value * density.density) / 2f
                                val hoverCol = ((centerX - stepPx / 2f) / stepPx)
                                    .roundToInt()
                                    .coerceIn(0, layout.columns - placement.spanX)
                                val hoverRow = ((centerY - stepPx / 2f) / stepPx)
                                    .roundToInt()
                                    .coerceAtLeast(0)

                                // Only repack when we've moved to a new cell
                                if (hoverCol != lastHoverCol || hoverRow != lastHoverRow) {
                                    lastHoverCol = hoverCol
                                    lastHoverRow = hoverRow
                                    draggedTargetCol = hoverCol
                                    draggedTargetRow = hoverRow

                                    // Repack only others around the dragged widget's target position
                                    val draggedWidget = placement.copy(
                                        col = hoverCol,
                                        row = hoverRow
                                    )
                                    val others = layout.placements.filter { it.id != placement.id }

                                    // Insert dragged first so it gets priority placement
                                    val reordered = listOf(draggedWidget) + others
                                    othersPacked = GridPacker.pack(reordered, layout.columns)
                                }
                            },
                            onDragEnd = {
                                onLayoutChanged(othersPacked)
                                draggingId = null
                                dragOffset = Offset.Zero
                                lastHoverCol = -1
                                lastHoverRow = -1
                                onDragStateChanged(false)
                            },
                            onDragCancel = {
                                othersPacked = layout.placements
                                draggingId = null
                                dragOffset = Offset.Zero
                                lastHoverCol = -1
                                lastHoverRow = -1
                                onDragStateChanged(false)
                            }
                        )
                    }
                } else Modifier

                // Dragged widget: follows finger freely. Others: animate to grid positions.
                val finalOffset = if (isDragging) {
                    IntOffset(
                        gridXPx + dragOffset.x.roundToInt(),
                        gridYPx + dragOffset.y.roundToInt()
                    )
                } else {
                    animatedOffset
                }

                Box(
                    modifier = Modifier
                        .offset { finalOffset }
                        .size(widgetWidth, widgetHeight)
                        .zIndex(if (isDragging) 10f else 0f)
                        .graphicsLayer {
                            if (isDragging) {
                                scaleX = 1.05f
                                scaleY = 1.05f
                                shadowElevation = 20f
                                alpha = 0.85f
                            }
                        }
                        .then(if (isEditMode && !isDragging) Modifier.wobble(index) else Modifier)
                        .then(dragModifier),
                    contentAlignment = Alignment.Center
                ) {
                    RenderWidget(
                        placement = placement,
                        data = data
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.wobble(index: Int): Modifier {
    val transition = rememberInfiniteTransition(label = "wobble_$index")
    val rotation by transition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 150 + (index % 3) * 30),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation_$index"
    )
    return this.graphicsLayer { rotationZ = rotation }
}
