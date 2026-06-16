package com.example.rampdispatch.data.session

import com.example.rampdispatch.domain.model.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds who is currently logged in, for the lifetime of the app process.
 * Exposes the session as a StateFlow so the UI can react to login/logout
 * the same way it reacts to data changes. No persistence in the MVP:
 * closing the app logs the user out (login screen on every launch).
 */
class SessionManager {

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

    fun login(user: CurrentUser) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
    }
}