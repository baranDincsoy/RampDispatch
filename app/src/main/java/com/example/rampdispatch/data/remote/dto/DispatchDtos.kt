package com.example.rampdispatch.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTOs mirror the remote JSON exactly.
 * They are kept separate from domain models on purpose:
 * the API contract can change without touching the rest of the app.
 */
@Serializable
data class DispatchResponseDto(
    val schemaVersion: Int,
    val station: String,
    val fuelers: List<FuelerDto>,
    val orders: List<FuelOrderDto>
)

@Serializable
data class FuelerDto(
    val id: String,
    val name: String,
    val deviceId: String
)

@Serializable
data class FuelOrderDto(
    val id: String,
    val flightNumber: String,
    val tailNumber: String,
    val airline: String,
    val terminal: String,
    val gate: String,
    val destination: String,
    val aircraftType: String,
    val fuelQuantityLbs: Int,
    val etaOffsetMin: Long,
    val etdOffsetMin: Long,
    val status: String,   // raw string; mapped to OrderStatus enum at the boundary
    val fuelerId: String?,
    val tanks: List<String> = emptyList()
)