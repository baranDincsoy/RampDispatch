package com.example.rampdispatch.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rampdispatch.RampDispatchApplication
import com.example.rampdispatch.data.repository.DispatchRepository
import com.example.rampdispatch.data.session.SessionManager
import com.example.rampdispatch.domain.model.CurrentUser
import com.example.rampdispatch.domain.model.Fueler
import com.example.rampdispatch.domain.model.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(
    private val sessionManager: SessionManager,
    private val repository: DispatchRepository
) : ViewModel() {

    /** The list of fuelers to offer as login options. */
    val fuelers: StateFlow<List<Fueler>> =
        repository.observeFuelers().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        // Pull data on app start so the login list is populated immediately.
        viewModelScope.launch {
            repository.refreshFromRemote()
        }
    }
    fun loginAsTeamLeader() {
        sessionManager.login(
            CurrentUser(displayName = "Team Leader", role = UserRole.TEAM_LEADER)
        )
    }

    fun loginAsFueler(fueler: Fueler) {
        sessionManager.login(
            CurrentUser(
                displayName = fueler.name,
                role = UserRole.FUELER,
                fuelerId = fueler.id
            )
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as RampDispatchApplication
                LoginViewModel(app.sessionManager, app.repository)
            }
        }
    }
}