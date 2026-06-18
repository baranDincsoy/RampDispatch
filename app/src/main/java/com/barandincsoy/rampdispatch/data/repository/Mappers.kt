package com.barandincsoy.rampdispatch.data.repository

import com.barandincsoy.rampdispatch.data.remote.dto.FuelOrderDto
import com.barandincsoy.rampdispatch.data.remote.dto.FuelerDto
import com.barandincsoy.rampdispatch.data.local.entity.FuelOrderEntity
import com.barandincsoy.rampdispatch.data.local.entity.FuelerEntity
import com.barandincsoy.rampdispatch.data.local.entity.StatusEventEntity
import com.barandincsoy.rampdispatch.domain.model.FuelOrder
import com.barandincsoy.rampdispatch.domain.model.Fueler
import com.barandincsoy.rampdispatch.domain.model.OrderStatus
import com.barandincsoy.rampdispatch.domain.model.StatusEvent
import java.time.Duration
import java.time.Instant
import com.barandincsoy.rampdispatch.domain.model.FuelTank

/**
 * Mapping happens at layer boundaries:
 * DTO -> Entity when syncing from remote (offsets become absolute times),
 * Entity -> Domain when exposing data to the rest of the app.
 */

fun FuelOrderDto.toEntity(syncTime: Instant): FuelOrderEntity =
    FuelOrderEntity(
        id = id,
        flightNumber = flightNumber,
        tailNumber = tailNumber,
        airline = airline,
        terminal = terminal,
        gate = gate,
        destination = destination,
        aircraftType = aircraftType,
        plannedQuantityLbs = fuelQuantityLbs,
        actualQuantityLbs = null,
        etaEpochMillis = syncTime.plus(Duration.ofMinutes(etaOffsetMin)).toEpochMilli(),
        etdEpochMillis = syncTime.plus(Duration.ofMinutes(etdOffsetMin)).toEpochMilli(),
        status = status.toOrderStatus(),
        fuelerId = fuelerId,
        tanksCsv = tanks.joinToString(",")
    )

fun FuelerDto.toEntity(): FuelerEntity =
    FuelerEntity(id = id, name = name, deviceId = deviceId)

fun FuelOrderEntity.toDomain(): FuelOrder =
    FuelOrder(
        id = id,
        flightNumber = flightNumber,
        tailNumber = tailNumber,
        airline = airline,
        terminal = terminal,
        gate = gate,
        destination = destination,
        aircraftType = aircraftType,
        plannedQuantityLbs = plannedQuantityLbs,
        actualQuantityLbs = actualQuantityLbs,
        eta = Instant.ofEpochMilli(etaEpochMillis),
        etd = Instant.ofEpochMilli(etdEpochMillis),
        status = status,
        fuelerId = fuelerId,
        tanks = tanksCsv.toFuelTanks()
    )

fun FuelerEntity.toDomain(): Fueler =
    Fueler(id = id, name = name, deviceId = deviceId)

fun StatusEventEntity.toDomain(): StatusEvent =
    StatusEvent(
        orderId = orderId,
        fromStatus = fromStatus,
        toStatus = toStatus,
        timestamp = Instant.ofEpochMilli(timestampMillis)
    )

/** Unknown statuses from the API degrade gracefully instead of crashing. */
private fun String.toOrderStatus(): OrderStatus =
    OrderStatus.entries.firstOrNull { it.name == this } ?: OrderStatus.PENDING

/** "LEFT,CENTER,RIGHT" -> [LEFT, CENTER, RIGHT]; unknown names are skipped. */
private fun String.toFuelTanks(): List<FuelTank> =
    if (isBlank()) emptyList()
    else split(",").mapNotNull { name ->
        FuelTank.entries.firstOrNull { it.name == name.trim() }
    }