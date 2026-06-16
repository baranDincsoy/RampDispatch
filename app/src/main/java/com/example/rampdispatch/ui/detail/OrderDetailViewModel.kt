package com.example.rampdispatch.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rampdispatch.RampDispatchApplication
import com.example.rampdispatch.data.repository.DispatchRepository
import com.example.rampdispatch.data.session.SessionManager
import com.example.rampdispatch.domain.model.FuelOrder
import com.example.rampdispatch.domain.model.Fueler
import com.example.rampdispatch.domain.model.StatusEvent
import com.example.rampdispatch.domain.model.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val order: FuelOrder? = null,
    val fuelers: List<Fueler> = emptyList(),
    val events: List<StatusEvent> = emptyList(),
    val canManageAssignment: Boolean = false,   // true only for team leader
    val isLoading: Boolean = true
)

class OrderDetailViewModel(
    private val repository: DispatchRepository,
    private val sessionManager: SessionManager,
    private val orderId: String
) : ViewModel() {

    val uiState: StateFlow<OrderDetailUiState> = combine(
        repository.observeOrder(orderId),
        repository.observeFuelers(),
        repository.observeStatusEvents(orderId)
    ) { order, fuelers, events ->
        OrderDetailUiState(
            order = order,
            fuelers = fuelers,
            events = events,
            canManageAssignment =
                sessionManager.currentUser.value?.role == UserRole.TEAM_LEADER,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OrderDetailUiState()
    )

    fun assignFueler(fuelerId: String) {
        val current = uiState.value.order ?: return
        viewModelScope.launch {
            repository.assignFueler(orderId, fuelerId, current.status)
        }
    }

    fun unassignFueler() {
        val current = uiState.value.order ?: return
        viewModelScope.launch {
            repository.unassignFueler(orderId, current.status)
        }
    }

    fun startFueling() {
        val current = uiState.value.order ?: return
        viewModelScope.launch {
            repository.startFueling(orderId, current.status)
        }
    }

    fun completeOrder(actualLbs: Int) {
        val current = uiState.value.order ?: return
        viewModelScope.launch {
            repository.completeOrder(orderId, actualLbs, current.status)
        }
    }

    companion object {
        /**
         * Factory is a function here, not a value: this ViewModel needs the
         * orderId, which is only known at navigation time.
         */
        fun factory(orderId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as RampDispatchApplication
                OrderDetailViewModel(app.repository, app.sessionManager, orderId)
            }
        }
    }
}