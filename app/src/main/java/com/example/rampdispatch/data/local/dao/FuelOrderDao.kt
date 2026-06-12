package com.example.rampdispatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rampdispatch.data.local.entity.FuelOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelOrderDao {

    /** Dispatch board: only active work, soonest departure first. */
    @Query("SELECT * FROM fuel_orders WHERE status != 'COMPLETED' ORDER BY etdEpochMillis ASC")
    fun observeActiveOrders(): Flow<List<FuelOrderEntity>>

    @Query("SELECT * FROM fuel_orders WHERE id = :orderId")
    fun observeOrder(orderId: String): Flow<FuelOrderEntity?>

    /**
     * IGNORE on conflict: a refresh from the server must never overwrite
     * status changes the user made locally.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(orders: List<FuelOrderEntity>)

    @Query("UPDATE fuel_orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateStatus(orderId: String, newStatus: String)

    @Query("UPDATE fuel_orders SET status = 'COMPLETED', actualQuantityLbs = :actualLbs WHERE id = :orderId")
    suspend fun completeOrder(orderId: String, actualLbs: Int)

    @Query("SELECT COUNT(*) FROM fuel_orders")
    suspend fun count(): Int
}