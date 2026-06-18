package com.barandincsoy.rampdispatch.domain.model

/**
 * The logged-in user. fuelerId is set only when role == FUELER,
 * so the board can filter to that fueler's own orders. A team leader
 * has no fuelerId — they see everything.
 */
data class CurrentUser(
    val displayName: String,
    val role: UserRole,
    val fuelerId: String? = null
)