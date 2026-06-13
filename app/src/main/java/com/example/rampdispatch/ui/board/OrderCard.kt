package com.example.rampdispatch.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.rampdispatch.domain.model.FuelOrder
import com.example.rampdispatch.ui.components.StatusChip
import com.example.rampdispatch.ui.components.asClockText
import com.example.rampdispatch.ui.components.asCountdownText
import com.example.rampdispatch.ui.components.statusVisuals
import com.example.rampdispatch.ui.theme.Dimens
import java.util.Locale

@Composable
fun OrderCard(
    order: FuelOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overdue = order.isOverdue()
    val (_, stripeColor, _) = statusVisuals(order.status, overdue)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {

            // Colored status stripe: readable from arm's length, even in sunlight.
            Box(
                Modifier
                    .width(Dimens.StatusStripeWidth)
                    .fillMaxHeight()
                    .background(stripeColor)
            )

            Column(Modifier.padding(Dimens.SpacingL)) {

                // Row 1: identity + status
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${order.flightNumber}  •  ${order.tailNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatusChip(status = order.status, isOverdue = overdue)
                }

                Spacer(Modifier.height(Dimens.SpacingS))

                // Row 2: where + what
                Text(
                    text = "Gate ${order.terminal}${order.gate}  •  ${order.aircraftType}  •  ${order.destination}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(Dimens.SpacingM))

                // Row 3: fuel + time
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format(Locale.US, "%,d lbs", order.plannedQuantityLbs),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "ETD ${order.etd.asClockText()}  (${order.timeUntilDeparture().asCountdownText()})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}