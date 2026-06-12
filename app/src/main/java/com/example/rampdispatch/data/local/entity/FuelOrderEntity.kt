package com.example.rampdispatch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.rampdispatch.domain.model.OrderStatus

@Entity(tableName = "fuel_orders")
data class FuelOrderEntity(
    @PrimaryKey val id: String,
    val flightNumber: String,
    val tailNumber: String,
    val airline: String,
    val terminal: String,
    val gate: String,
    val destination: String,
    val aircraftType: String,
    val plannedQuantityLbs: Int,
    val actualQuantityLbs: Int?,
    val etaEpochMillis: Long,
    val etdEpochMillis: Long,
    val status: OrderStatus,
    val fuelerId: String?
)