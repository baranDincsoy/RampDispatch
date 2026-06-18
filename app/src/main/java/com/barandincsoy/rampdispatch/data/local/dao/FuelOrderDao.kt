package com.barandincsoy.rampdispatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barandincsoy.rampdispatch.data.local.entity.FuelOrderEntity
import kotlinx.coroutines.flow.Flow


data class FuelerWorkload(
    val fuelerId: String,
    val completedCount: Int,
    val totalLbs: Long
)

@Dao
interface FuelOrderDao {

    @Query("""
        SELECT fuelerId AS fuelerId,
               COUNT(*) AS completedCount,
               COALESCE(SUM(actualQuantityLbs), 0) AS totalLbs
        FROM fuel_orders
        WHERE status = 'COMPLETED' AND fuelerId IS NOT NULL
        GROUP BY fuelerId
    """)
    fun observeFuelerWorkloads(): Flow<List<FuelerWorkload>>

    /** Dispatch board: only active work, soonest departure first. */
    @Query("SELECT * FROM fuel_orders WHERE status != 'COMPLETED' ORDER BY etdEpochMillis ASC")
    fun observeActiveOrders(): Flow<List<FuelOrderEntity>>

    /** A fueler sees only their own active work. */
    @Query("SELECT * FROM fuel_orders WHERE status != 'COMPLETED' AND fuelerId = :fuelerId ORDER BY etdEpochMillis ASC")
    fun observeActiveOrdersForFueler(fuelerId: String): Flow<List<FuelOrderEntity>>

    @Query("SELECT * FROM fuel_orders WHERE id = :orderId")
    fun observeOrder(orderId: String): Flow<FuelOrderEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(orders: List<FuelOrderEntity>)

    @Query("UPDATE fuel_orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateStatus(orderId: String, newStatus: String)

    @Query("UPDATE fuel_orders SET status = 'COMPLETED', actualQuantityLbs = :actualLbs WHERE id = :orderId")
    suspend fun completeOrder(orderId: String, actualLbs: Int)

    @Query("UPDATE fuel_orders SET fuelerId = :fuelerId, status = 'ASSIGNED' WHERE id = :orderId")
    suspend fun assignFueler(orderId: String, fuelerId: String)

    @Query("UPDATE fuel_orders SET fuelerId = NULL, status = 'PENDING' WHERE id = :orderId")
    suspend fun unassignFueler(orderId: String)

    @Query("SELECT id FROM fuel_orders")
    suspend fun getAllIds(): List<String>

    @Query("SELECT COUNT(*) FROM fuel_orders")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM fuel_orders WHERE status = 'COMPLETED'")
    fun observeCompletedCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(actualQuantityLbs), 0) FROM fuel_orders WHERE status = 'COMPLETED'")
    fun observeTotalFueledLbs(): Flow<Long>

    @Query("SELECT COUNT(*) FROM fuel_orders WHERE status != 'COMPLETED'")
    fun observeActiveCount(): Flow<Int>
}


