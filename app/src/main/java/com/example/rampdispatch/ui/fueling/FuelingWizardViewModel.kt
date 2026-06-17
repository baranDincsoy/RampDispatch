package com.example.rampdispatch.ui.fueling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rampdispatch.RampDispatchApplication
import com.example.rampdispatch.data.repository.DispatchRepository
import com.example.rampdispatch.domain.model.FuelOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Tolerance band (lbs) for both final reading and totalizer reconciliation. */
private const val TOLERANCE_LBS = 200

data class FuelingUiState(
    val order: FuelOrder? = null,
    val step: FuelingStep = FuelingStep.TAIL_VERIFY,
    val data: FuelingData = FuelingData(),
    val isComplete: Boolean = false   // set true after close-out, to trigger navigation back
) {
    /** Target amount to reach: planned total minus what's already on board. */
    val neededLbs: Int?
        get() {
            val planned = order?.plannedQuantityLbs ?: return null
            val arrival = data.arrivalLbs ?: return null
            return (planned - arrival).coerceAtLeast(0)
        }

    /** Whether the current step's input is valid enough to advance. */
    val canAdvance: Boolean
        get() = when (step) {
            FuelingStep.TAIL_VERIFY ->
                order != null && data.enteredTail.trim().equals(order.tailNumber, ignoreCase = true)
            FuelingStep.EQUIPMENT ->
                data.equipmentNumber.isNotBlank()
            FuelingStep.ARRIVAL ->
                data.arrivalLbs != null
            FuelingStep.PUMPING ->
                true   // pumping is an action, not an input
            FuelingStep.FINAL_READING ->
                data.finalLbs != null
            FuelingStep.CAP_CHECK ->
                data.capConfirmed
            FuelingStep.TOTALIZER ->
                data.totalizerLbs != null && isTotalizerReconciled
            FuelingStep.CLOSEOUT ->
                data.employeeId.isNotBlank()
        }

    /** Final panel reading is within tolerance of the planned total. */
    val isFinalWithinTolerance: Boolean
        get() {
            val planned = order?.plannedQuantityLbs ?: return false
            val finalLbs = data.finalLbs ?: return false
            return kotlin.math.abs(finalLbs - planned) <= TOLERANCE_LBS
        }

    /** Cart totalizer agrees with the panel final reading, within tolerance. */
    val isTotalizerReconciled: Boolean
        get() {
            val finalLbs = data.finalLbs ?: return false
            val totalizer = data.totalizerLbs ?: return false
            return kotlin.math.abs(totalizer - finalLbs) <= TOLERANCE_LBS
        }
}

class FuelingWizardViewModel(
    private val repository: DispatchRepository,
    private val orderId: String
) : ViewModel() {

    private val step = MutableStateFlow(FuelingStep.TAIL_VERIFY)
    private val data = MutableStateFlow(FuelingData())
    private val isComplete = MutableStateFlow(false)

    val uiState: StateFlow<FuelingUiState> = combine(
        repository.observeOrder(orderId),
        step,
        data,
        isComplete
    ) { order, currentStep, currentData, complete ->
        FuelingUiState(
            order = order,
            step = currentStep,
            data = currentData,
            isComplete = complete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FuelingUiState()
    )

    // --- Step navigation ---

    fun goNext() {
        val current = uiState.value
        if (!current.canAdvance) return

        // Side effects that happen on entering certain steps:
        when (current.step) {
            FuelingStep.PUMPING -> startPump()       // mark IN_PROGRESS when leaving PUMPING
            FuelingStep.CLOSEOUT -> { completeOrder(); return }
            else -> Unit
        }
        step.value = current.step.next()
    }

    fun goBack() {
        step.value = step.value.previous()
    }

    // --- Field updates (UI calls these as the fueler types) ---

    fun onTailChanged(value: String) { data.value = data.value.copy(enteredTail = value) }
    fun onEquipmentChanged(value: String) { data.value = data.value.copy(equipmentNumber = value) }
    fun onArrivalChanged(value: Int?) { data.value = data.value.copy(arrivalLbs = value) }
    fun onFinalChanged(value: Int?) { data.value = data.value.copy(finalLbs = value) }
    fun onCapConfirmedChanged(value: Boolean) { data.value = data.value.copy(capConfirmed = value) }
    fun onTotalizerChanged(value: Int?) { data.value = data.value.copy(totalizerLbs = value) }
    fun onEmployeeIdChanged(value: String) { data.value = data.value.copy(employeeId = value) }

    // --- Persistence (only at the boundaries) ---

    private fun startPump() {
        val order = uiState.value.order ?: return
        viewModelScope.launch {
            repository.startFueling(orderId, order.status)
        }
    }

    private fun completeOrder() {
        val current = uiState.value
        val order = current.order ?: return
        val finalLbs = current.data.finalLbs ?: return
        viewModelScope.launch {
            repository.completeOrder(orderId, finalLbs, order.status)
            isComplete.value = true
        }
    }

    companion object {
        fun factory(orderId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as RampDispatchApplication
                FuelingWizardViewModel(app.repository, orderId)
            }
        }
    }
}