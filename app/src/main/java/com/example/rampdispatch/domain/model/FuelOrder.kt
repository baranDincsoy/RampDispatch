package com.example.rampdispatch.domain.model

import java.time.Duration
import java.time.Instant

data class FuelOrder(
    val id: String,
    val flightNumber: String,
    val tailNumber: String,
    val airline: String,
    val terminal: String,
    val gate: String,
    val destination: String,
    val aircraftType: String,
    val plannedQuantityLbs: Int,
    val actualQuantityLbs: Int?,   // entered by the fueler on completion
    val eta: Instant,
    val etd: Instant,
    val status: OrderStatus,
    val fuelerId: String?,
    val tanks: List<FuelTank>
) {

    /** An order is overdue when its departure time has passed but it is not completed. */
    fun isOverdue(now: Instant = Instant.now()): Boolean =
        status != OrderStatus.COMPLETED && now.isAfter(etd)

    /** Time the aircraft spends on the ground (ETA → ETD). */
    val groundTime: Duration
        get() = Duration.between(eta, etd)

    /** Remaining time until departure; negative when overdue. */
    fun timeUntilDeparture(now: Instant = Instant.now()): Duration =
        Duration.between(now, etd)
}