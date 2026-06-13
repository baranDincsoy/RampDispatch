package com.example.rampdispatch.ui.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rampdispatch.RampDispatchApplication
import com.example.rampdispatch.data.repository.DispatchRepository
import com.example.rampdispatch.domain.model.FuelOrder
import com.example.rampdispatch.domain.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Filter chips shown on top of the board. */
enum class BoardFilter { ALL, PENDING, ASSIGNED, IN_PROGRESS, OVERDUE }

/** One immutable snapshot of everything the board screen needs to draw. */
data class BoardUiState(
    val orders: List<FuelOrder> = emptyList(),
    val selectedFilter: BoardFilter = BoardFilter.ALL,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class DispatchBoardViewModel(
    private val repository: DispatchRepository
) : ViewModel() {

    // Private, mutable inputs — only the ViewModel can change these.
    private val selectedFilter = MutableStateFlow(BoardFilter.ALL)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    /**
     * Public, read-only output. combine() re-computes the UiState whenever
     * ANY of its inputs emits: a Room change, a filter tap, a refresh flag.
     */
    val uiState: StateFlow<BoardUiState> = combine(
        repository.observeActiveOrders(),
        selectedFilter,
        isRefreshing,
        errorMessage
    ) { orders, filter, refreshing, error ->
        BoardUiState(
            orders = orders.applyFilter(filter),
            selectedFilter = filter,
            isRefreshing = refreshing,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BoardUiState()
    )

    init {
        refresh()   // pull fresh data once when the screen first opens
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            repository.refreshFromRemote().onFailure {
                errorMessage.value = "Couldn't refresh — showing cached data."
            }
            isRefreshing.value = false
        }
    }

    fun onFilterSelected(filter: BoardFilter) {
        selectedFilter.value = filter
    }

    /** Called by the UI after the error has been shown once. */
    fun onErrorShown() {
        errorMessage.value = null
    }

    private fun List<FuelOrder>.applyFilter(filter: BoardFilter): List<FuelOrder> =
        when (filter) {
            BoardFilter.ALL -> this
            BoardFilter.PENDING -> filter { it.status == OrderStatus.PENDING }
            BoardFilter.ASSIGNED -> filter { it.status == OrderStatus.ASSIGNED }
            BoardFilter.IN_PROGRESS -> filter { it.status == OrderStatus.IN_PROGRESS }
            BoardFilter.OVERDUE -> filter { it.isOverdue() }
        }

    companion object {
        /** Tells Android how to build this ViewModel with its dependency. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as RampDispatchApplication
                DispatchBoardViewModel(app.repository)
            }
        }
    }
}