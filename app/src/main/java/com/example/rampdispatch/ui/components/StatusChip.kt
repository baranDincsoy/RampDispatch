package com.example.rampdispatch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rampdispatch.domain.model.OrderStatus
import com.example.rampdispatch.ui.theme.*

/**
 * Status badge. Overdue overrides the stored status visually —
 * it is a derived alert, the most important signal on the board.
 */
@Composable
fun StatusChip(
    status: OrderStatus,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    val (label, contentColor, containerColor) = statusVisuals(status, isOverdue)

    Text(
        text = label,
        color = contentColor,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(containerColor, RoundedCornerShape(Dimens.ChipCornerRadius))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

fun statusVisuals(status: OrderStatus, isOverdue: Boolean): Triple<String, Color, Color> =
    when {
        isOverdue -> Triple("OVERDUE", StatusOverdue, StatusOverdueContainer)
        status == OrderStatus.PENDING -> Triple("PENDING", StatusPending, StatusPendingContainer)
        status == OrderStatus.ASSIGNED -> Triple("ASSIGNED", StatusAssigned, StatusAssignedContainer)
        status == OrderStatus.IN_PROGRESS -> Triple("IN PROGRESS", StatusInProgress, StatusInProgressContainer)
        else -> Triple("COMPLETED", StatusCompleted, StatusCompletedContainer)
    }