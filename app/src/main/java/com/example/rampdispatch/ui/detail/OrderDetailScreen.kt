package com.example.rampdispatch.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rampdispatch.domain.model.FuelOrder
import com.example.rampdispatch.domain.model.Fueler
import com.example.rampdispatch.domain.model.OrderStatus
import com.example.rampdispatch.domain.model.StatusEvent
import com.example.rampdispatch.ui.components.StatusChip
import com.example.rampdispatch.ui.components.asClockText
import com.example.rampdispatch.ui.theme.Dimens
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel =
        viewModel(factory = OrderDetailViewModel.factory(orderId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.order?.flightNumber ?: "Order Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        val order = uiState.order

        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            order == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Order not found") }

            else -> LazyColumn(
                Modifier.padding(innerPadding),
                contentPadding = PaddingValues(Dimens.SpacingL),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL)
            ) {
                item { OrderHeader(order) }
                item {
                    ActionSection(
                        order = order,
                        fuelers = uiState.fuelers,
                        canManage = uiState.canManageAssignment,
                        onAssign = viewModel::assignFueler,
                        onStart = viewModel::startFueling,
                        onUnassign = viewModel::unassignFueler,
                        onComplete = viewModel::completeOrder
                    )
                }
                item {
                    Text(
                        "Status history",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(uiState.events) { event -> TimelineRow(event) }
            }
        }
    }
}

@Composable
private fun OrderHeader(order: FuelOrder) {
    Card {
        Column(Modifier.padding(Dimens.SpacingL)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${order.flightNumber}  •  ${order.tailNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(order.status, order.isOverdue())
            }
            Spacer(Modifier.height(Dimens.SpacingM))
            DetailLine("Gate", "${order.terminal}${order.gate}")
            DetailLine("Aircraft", "${order.aircraftType}  →  ${order.destination}")
            DetailLine("Planned fuel", String.format(Locale.US, "%,d lbs", order.plannedQuantityLbs))
            order.actualQuantityLbs?.let {
                DetailLine("Actual fuel", String.format(Locale.US, "%,d lbs", it))
            }
            DetailLine("ETA", order.eta.asClockText())
            DetailLine("ETD", order.etd.asClockText())
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Dimens.SpacingXs)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * The action area is a state machine in UI form:
 * what the user can do depends entirely on the current status.
 */
@Composable
private fun ActionSection(
    order: FuelOrder,
    fuelers: List<Fueler>,
    canManage: Boolean,
    onAssign: (String) -> Unit,
    onStart: () -> Unit,
    onUnassign: () -> Unit,
    onComplete: (Int) -> Unit
) {
    when (order.status) {

        OrderStatus.PENDING -> {
            if (canManage) {
                var showPicker by remember { mutableStateOf(false) }
                Button(
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget)
                ) { Text("Assign fueler") }

                if (showPicker) {
                    FuelerPickerDialog(
                        fuelers = fuelers,
                        onPick = { fuelerId ->
                            showPicker = false
                            onAssign(fuelerId)
                        },
                        onDismiss = { showPicker = false }
                    )
                }
            } else {
                Text(
                    "Waiting for assignment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OrderStatus.ASSIGNED -> {
            var showReassign by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget)
                ) { Text("Start fueling") }

                if (canManage) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
                        OutlinedButton(
                            onClick = { showReassign = true },
                            modifier = Modifier.weight(1f).height(Dimens.MinTouchTarget)
                        ) { Text("Reassign") }

                        OutlinedButton(
                            onClick = onUnassign,
                            modifier = Modifier.weight(1f).height(Dimens.MinTouchTarget)
                        ) { Text("Unassign") }
                    }
                }
            }

            if (showReassign) {
                FuelerPickerDialog(
                    fuelers = fuelers,
                    onPick = { fuelerId ->
                        showReassign = false
                        onAssign(fuelerId)
                    },
                    onDismiss = { showReassign = false }
                )
            }
        }

        OrderStatus.IN_PROGRESS -> {
            var quantityText by remember { mutableStateOf("") }
            val quantity = quantityText.toIntOrNull()

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter(Char::isDigit) },
                    label = { Text("Actual quantity (lbs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { quantity?.let(onComplete) },
                    enabled = quantity != null && quantity > 0,
                    modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget)
                ) { Text("Complete order") }
            }
        }

        OrderStatus.COMPLETED -> Text(
            "Order completed.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FuelerPickerDialog(
    fuelers: List<Fueler>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign fueler") },
        text = {
            Column {
                fuelers.forEach { fueler ->
                    TextButton(
                        onClick = { onPick(fueler.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${fueler.name}  •  ${fueler.deviceId}")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TimelineRow(event: StatusEvent) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = if (event.fromStatus == null) "Created as ${event.toStatus}"
            else "${event.fromStatus} → ${event.toStatus}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = event.timestamp.asClockText(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}