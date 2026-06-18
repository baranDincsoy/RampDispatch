package com.barandincsoy.rampdispatch.ui.fueling

import androidx.compose.foundation.layout.*
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
import com.barandincsoy.rampdispatch.domain.model.FuelOrder
import com.barandincsoy.rampdispatch.ui.theme.Dimens
import java.util.Locale
import com.barandincsoy.rampdispatch.domain.model.FuelTank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelingWizardScreen(
    orderId: String,
    onFinished: () -> Unit,
    onCancel: () -> Unit,
    viewModel: FuelingWizardViewModel =
        viewModel(factory = FuelingWizardViewModel.factory(orderId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // When close-out succeeds, leave the wizard.
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onFinished()
    }

    val order = uiState.order
    val stepNumber = uiState.step.ordinal + 1
    val totalSteps = FuelingStep.entries.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fueling — step $stepNumber of $totalSteps") },
                navigationIcon = {
                    IconButton(onClick = { if (uiState.step.isFirst) onCancel() else viewModel.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                isLast = uiState.step.isLast,
                canAdvance = uiState.canAdvance,
                onNext = viewModel::goNext
            )
        }
    ) { innerPadding ->
        if (order == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.SpacingL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL)
        ) {
            LinearProgressIndicator(
                progress = { stepNumber.toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth()
            )

            // Body changes per step; the frame around it stays the same.
            when (uiState.step) {
                FuelingStep.TAIL_VERIFY -> TailVerifyStep(uiState, order, viewModel)
                FuelingStep.EQUIPMENT -> EquipmentStep(uiState, viewModel)
                FuelingStep.ARRIVAL -> ArrivalStep(uiState, order, viewModel)
                FuelingStep.PUMPING -> PumpingStep(uiState, order)
                FuelingStep.FINAL_READING -> FinalReadingStep(uiState, order, viewModel)
                FuelingStep.CAP_CHECK -> CapCheckStep(uiState, viewModel)
                FuelingStep.TOTALIZER -> TotalizerStep(uiState, viewModel)
                FuelingStep.CLOSEOUT -> CloseoutStep(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun BottomBar(isLast: Boolean, canAdvance: Boolean, onNext: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Button(
            onClick = onNext,
            enabled = canAdvance,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingL)
                .height(Dimens.MinTouchTarget)
        ) {
            Text(if (isLast) "Complete order" else "Next")
        }
    }
}

/* ---------- Per-step bodies ---------- */

@Composable
private fun StepTitle(title: String, hint: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TailVerifyStep(state: FuelingUiState, order: FuelOrder, vm: FuelingWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
        StepTitle("Verify aircraft", "Enter the tail number on the aircraft to confirm you're at the right one.")

        // Context so the fueler knows which order this is — without revealing
        // the exact tail (they must read it off the aircraft).
        InfoLine("Flight", order.flightNumber)
        InfoLine("Gate", "${order.terminal}${order.gate}")
        InfoLine("Destination", order.destination)

        OutlinedTextField(
            value = state.data.enteredTail,
            onValueChange = vm::onTailChanged,
            label = { Text("Tail number") },
            singleLine = true,
            isError = state.data.enteredTail.isNotBlank() && !state.canAdvance,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.data.enteredTail.isNotBlank() && !state.canAdvance) {
            Text("Doesn't match this order.", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EquipmentStep(state: FuelingUiState, vm: FuelingWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
        StepTitle("Equipment", "Enter the fuel cart or unit number you're using.")
        OutlinedTextField(
            value = state.data.equipmentNumber,
            onValueChange = vm::onEquipmentChanged,
            label = { Text("Cart / unit number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ArrivalStep(state: FuelingUiState, order: FuelOrder, vm: FuelingWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
        StepTitle("Arrival fuel", "Enter the fuel already on board, per tank.")

        order.tanks.forEach { tank ->
            TankField(
                tank = tank,
                value = state.data.arrivalByTank[tank],
                onChange = { vm.onArrivalTankChanged(tank, it) }
            )
        }

        InfoLine("Arrival total", String.format(Locale.US, "%,d lbs", state.data.arrivalTotalLbs))
        InfoLine("Planned total", String.format(Locale.US, "%,d lbs", order.plannedQuantityLbs))
        state.neededLbs?.let {
            InfoLine("Need to add", String.format(Locale.US, "%,d lbs", it), highlight = true)
        }
    }
}

@Composable
private fun PumpingStep(state: FuelingUiState, order: FuelOrder) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
        StepTitle("Ready to pump", "Confirm to signal the cart and begin fueling.")
        InfoLine("Target to add", state.neededLbs?.let { String.format(Locale.US, "%,d lbs", it) } ?: "—", highlight = true)
        Text(
            "Tapping Next starts the pump and marks this order in progress.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FinalReadingStep(state: FuelingUiState, order: FuelOrder, vm: FuelingWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
        StepTitle("Final reading", "Enter the fuel in each tank after fueling.")

        order.tanks.forEach { tank ->
            TankField(
                tank = tank,
                value = state.data.finalByTank[tank],
                onChange = { vm.onFinalTankChanged(tank, it) }
            )
        }

        InfoLine("Final total", String.format(Locale.US, "%,d lbs", state.data.finalTotalLbs))
        InfoLine("Planned total", String.format(Locale.US, "%,d lbs", order.plannedQuantityLbs))
        if (state.data.finalByTank.isNotEmpty() && !state.isFinalWithinTolerance) {
            Text("Total is outside ±200 lbs of planned — re-check.",
                color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CapCheckStep(state: FuelingUiState, vm: FuelingWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
        StepTitle("Safety check")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
            Checkbox(checked = state.data.capConfirmed, onCheckedChange = vm::onCapConfirmedChanged)
            Text("Fuel cap is securely back on", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun TotalizerStep(state: FuelingUiState, vm: FuelingWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
        StepTitle("Totalizer", "Enter the cart's gallon counter before and after fueling.")

        LbsField(
            value = state.data.totalizerStartGal,
            label = "Totalizer start (gal)",
            onChange = vm::onTotalizerStartChanged
        )
        LbsField(
            value = state.data.totalizerEndGal,
            label = "Totalizer end (gal)",
            onChange = vm::onTotalizerEndChanged
        )
        state.data.gallonsPumped?.let { pumped ->
            InfoLine(
                "Gallons pumped",
                String.format(Locale.US, "%,d gal", pumped),
                highlight = true
            )
            if (pumped <= 0) {
                Text("End must be greater than start.",
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CloseoutStep(state: FuelingUiState, vm: FuelingWizardViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
        StepTitle("Close out", "Enter your employee ID to complete the order.")
        OutlinedTextField(
            value = state.data.employeeId,
            onValueChange = vm::onEmployeeIdChanged,
            label = { Text("Employee ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TankField(tank: FuelTank, value: Int?, onChange: (Int?) -> Unit) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { text -> onChange(text.filter(Char::isDigit).toIntOrNull()) },
        label = { Text("${tank.label} (lbs)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LbsField(value: Int?, label: String, onChange: (Int?) -> Unit) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { text -> onChange(text.filter(Char::isDigit).toIntOrNull()) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun InfoLine(label: String, value: String, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}