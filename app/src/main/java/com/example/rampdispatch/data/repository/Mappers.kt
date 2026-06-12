package com.example.rampdispatch.data.repository

import com.example.rampdispatch.data.remote.dto.FuelOrderDto
import com.example.rampdispatch.data.remote.dto.FuelerDto
import com.example.rampdispatch.data.local.entity.FuelOrderEntity
import com.example.rampdispatch.data.local.entity.FuelerEntity
import com.example.rampdispatch.data.local.entity.StatusEventEntity
import com.example.rampdispatch.domain.model.FuelOrder
import com.example.rampdispatch.domain.model.Fueler
import com.example.rampdispatch.domain.model.OrderStatus
import com.example.rampdispatch.domain.model.StatusEvent
import java.time.Duration
import java.time.Instant

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
        fuelerId = fuelerId
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
        fuelerId = fuelerId
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