package com.example.rampdispatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.rampdispatch.data.local.entity.StatusEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusEventDao {

    @Query("SELECT * FROM status_events WHERE orderId = :orderId ORDER BY timestampMillis ASC")
    fun observeEventsForOrder(orderId: String): Flow<List<StatusEventEntity>>

    @Insert
    suspend fun insert(event: StatusEventEntity)
}