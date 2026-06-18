package com.barandincsoy.rampdispatch.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.barandincsoy.rampdispatch.RampDispatchApplication
import com.barandincsoy.rampdispatch.data.repository.DispatchRepository
import com.barandincsoy.rampdispatch.domain.model.Fueler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** A fueler paired with their completed-work totals, ready for display. */
data class FuelerStat(
    val name: String,
    val deviceId: String,
    val completedCount: Int,
    val totalLbs: Long
)

data class StatsUiState(
    val completedCount: Int = 0,
    val activeCount: Int = 0,
    val totalFueledLbs: Long = 0,
    val fuelerStats: List<FuelerStat> = emptyList()
)

class StatsViewModel(
    repository: DispatchRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.observeCompletedCount(),
        repository.observeActiveCount(),
        repository.observeTotalFueledLbs(),
        repository.observeFuelerWorkloads(),
        repository.observeFuelers()
    ) { completed, active, totalLbs, workloads, fuelers ->

        // Join workloads (keyed by id) with fueler names for display.
        val byId: Map<String, Fueler> = fuelers.associateBy { it.id }
        val stats = workloads.mapNotNull { w ->
            byId[w.fuelerId]?.let { fueler ->
                FuelerStat(
                    name = fueler.name,
                    deviceId = fueler.deviceId,
                    completedCount = w.completedCount,
                    totalLbs = w.totalLbs
                )
            }
        }.sortedByDescending { it.totalLbs }

        StatsUiState(
            completedCount = completed,
            activeCount = active,
            totalFueledLbs = totalLbs,
            fuelerStats = stats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as RampDispatchApplication
                StatsViewModel(app.repository)
            }
        }
    }
}