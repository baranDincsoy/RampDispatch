package com.example.rampdispatch.ui.fueling

import com.example.rampdispatch.domain.model.FuelTank

/**
 * Accumulates everything the fueler enters during the wizard.
 * Held in memory only; persisted to Room just once, at close-out.
 *
 * Arrival and final fuel are per-tank maps (FuelTank -> lbs), because the
 * number of tanks depends on the aircraft. Totalizer is in GALLONS — it's a
 * running counter on the cart, so we capture start and end and the difference
 * is the gallons pumped.
 */
data class FuelingData(
    val enteredTail: String = "",
    val equipmentNumber: String = "",
    val arrivalByTank: Map<FuelTank, Int> = emptyMap(),
    val finalByTank: Map<FuelTank, Int> = emptyMap(),
    val capConfirmed: Boolean = false,
    val totalizerStartGal: Int? = null,
    val totalizerEndGal: Int? = null,
    val employeeId: String = ""
) {
    /** Sum of all arrival tank readings. */
    val arrivalTotalLbs: Int get() = arrivalByTank.values.sum()

    /** Sum of all final tank readings — the total fuel on board after fueling. */
    val finalTotalLbs: Int get() = finalByTank.values.sum()

    /** Gallons pumped = end counter - start counter. */
    val gallonsPumped: Int?
        get() {
            val start = totalizerStartGal ?: return null
            val end = totalizerEndGal ?: return null
            return end - start
        }
}