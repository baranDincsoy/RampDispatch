package com.barandincsoy.rampdispatch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.barandincsoy.rampdispatch.domain.model.OrderStatus

@Entity(tableName = "status_events")
data class StatusEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val fromStatus: OrderStatus?,   // null = initial state when first synced
    val toStatus: OrderStatus,
    val timestampMillis: Long
)