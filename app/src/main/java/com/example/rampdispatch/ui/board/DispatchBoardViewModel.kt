package com.example.rampdispatch.ui.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rampdispatch.RampDispatchApplication
import com.example.rampdispatch.data.repository.DispatchRepository
import com.example.rampdispatch.data.session.SessionManager
import com.example.rampdispatch.domain.model.FuelOrder
import com.example.rampdispatch.domain.model.OrderStatus
import com.example.rampdispatch.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



/** How the board is ordered. */
enum class BoardSort(val label: String) {
    ETD("ETD"),
    GATE("Gate"),
    FUELER("Fueler")
}

/** Filter chips shown on top of the board. */
enum class BoardFilter { ALL, PENDING, ASSIGNED, IN_PROGRESS, OVERDUE }

/** One immutable snapshot of everything the board screen needs to draw. */

// Team leader sees all orders; a fueler sees only their own.

data class BoardUiState(
    val items: List<BoardItem> = emptyList(),
    val selectedFilter: BoardFilter = BoardFilter.ALL,
    val selectedSort: BoardSort = BoardSort.ETD,
    val selectedConcourse: String? = null,        // null = All concourses
    val availableConcourses: List<String> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class DispatchBoardViewModel(
    private val repository: DispatchRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Private, mutable inputs — only the ViewModel can change these.
    private val selectedSort = MutableStateFlow(BoardSort.ETD)
    private val selectedFilter = MutableStateFlow(BoardFilter.ALL)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val selectedConcourse = MutableStateFlow<String?>(null)


    private val ordersSource: Flow<List<FuelOrder>> = run {
        val user = sessionManager.currentUser.value
        if (user?.role == UserRole.FUELER && user.fuelerId != null) {
            repository.observeActiveOrdersForFueler(user.fuelerId)
        } else {
            repository.observeActiveOrders()
        }
    }
    /**
     * Public, read-only output. combine() re-computes the UiState whenever
     * ANY of its inputs emits: a Room change, a filter tap, a refresh flag.
     */
    val uiState: StateFlow<BoardUiState> = combine(
        ordersSource,                          // was: repository.observeActiveOrders()
        repository.observeFuelers(),
        selectedFilter,
        selectedSort,
        combine(selectedConcourse, isRefreshing, errorMessage) { c, r, e -> Triple(c, r, e) }
    ) { orders, fuelers, filter, sort, (concourse, refreshing, error) ->

        val nameById = fuelers.associateBy({ it.id }, { it.name })

        // Concourse chips are derived from the data, sorted alphabetically.
        val concourses = orders.map { it.terminal }.distinct().sorted()

        val items = orders
            .applyFilter(filter)
            .filter { concourse == null || it.terminal == concourse }   // concourse filter
            .map { order -> BoardItem(order, order.fuelerId?.let { nameById[it] }) }
            .applySort(sort)

        BoardUiState(
            items = items,
            selectedFilter = filter,
            selectedSort = sort,
            selectedConcourse = concourse,
            availableConcourses = concourses,
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
    fun onSortSelected(sort: BoardSort) {
        selectedSort.value = sort
    }
    fun onConcourseSelected(concourse: String?) {
        selectedConcourse.value = concourse
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
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as RampDispatchApplication
                DispatchBoardViewModel(app.repository, app.sessionManager)
            }
        }
    }
}

private fun List<BoardItem>.applySort(sort: BoardSort): List<BoardItem> =
    when (sort) {
        BoardSort.ETD ->
            sortedBy { it.order.etd }

        BoardSort.GATE ->
            sortedWith(compareBy({ it.order.terminal }, { it.order.gate }))

        BoardSort.FUELER ->
            sortedWith(
                compareBy<BoardItem, String?>(nullsLast()) { it.fuelerName }
                    .thenBy { it.order.etd }
            )
    }