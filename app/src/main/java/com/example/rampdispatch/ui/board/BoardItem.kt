package com.example.rampdispatch.ui.board

import com.example.rampdispatch.domain.model.FuelOrder

/**
 * What a single board row needs to draw: the order plus the resolved
 * fueler name. The domain FuelOrder only knows fuelerId; the name lives
 * in another table, so we join them here for display.
 */
data class BoardItem(
    val order: FuelOrder,
    val fuelerName: String?   // null while PENDING (no fueler yet)
)