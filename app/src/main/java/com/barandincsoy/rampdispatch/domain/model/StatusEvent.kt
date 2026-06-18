package com.barandincsoy.rampdispatch.domain.model

import java.time.Instant

data class StatusEvent(
    val orderId: String,
    val fromStatus: OrderStatus?,
    val toStatus: OrderStatus,
    val timestamp: Instant
)