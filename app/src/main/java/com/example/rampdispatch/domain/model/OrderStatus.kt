package com.example.rampdispatch.domain.model

/**
 * Lifecycle of a fuel order.
 * Note: "Overdue" is intentionally NOT a status — it is a derived state,
 * computed from the current time vs. ETD (see FuelOrder.isOverdue).
 */
enum class OrderStatus {
    PENDING,      // no fueler assigned yet
    ASSIGNED,     // fueler assigned, fueling not started
    IN_PROGRESS,  // fueling in progress
    COMPLETED     // closed by the fueler; removed from the dispatch board
}