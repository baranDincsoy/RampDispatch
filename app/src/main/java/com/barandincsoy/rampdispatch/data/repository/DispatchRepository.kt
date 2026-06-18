package com.barandincsoy.rampdispatch.data.repository

import com.barandincsoy.rampdispatch.data.local.dao.FuelOrderDao
import com.barandincsoy.rampdispatch.data.local.dao.FuelerDao
import com.barandincsoy.rampdispatch.data.local.dao.StatusEventDao
import com.barandincsoy.rampdispatch.data.local.entity.StatusEventEntity
import com.barandincsoy.rampdispatch.data.remote.DispatchApi
import com.barandincsoy.rampdispatch.domain.model.FuelOrder
import com.barandincsoy.rampdispatch.domain.model.Fueler
import com.barandincsoy.rampdispatch.domain.model.OrderStatus
import com.barandincsoy.rampdispatch.domain.model.StatusEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import com.barandincsoy.rampdispatch.data.local.dao.FuelerWorkload
/**
 * Single source of truth: the UI always observes Room.
 * The remote API only feeds Room; it never reaches the UI directly.
 */
class DispatchRepository(
    private val api: DispatchApi,
    private val orderDao: FuelOrderDao,
    private val fuelerDao: FuelerDao,
    private val eventDao: StatusEventDao
) {

    fun observeCompletedCount(): Flow<Int> = orderDao.observeCompletedCount()
    fun observeActiveCount(): Flow<Int> = orderDao.observeActiveCount()
    fun observeTotalFueledLbs(): Flow<Long> = orderDao.observeTotalFueledLbs()
    fun observeFuelerWorkloads(): Flow<List<FuelerWorkload>> = orderDao.observeFuelerWorkloads()

    fun observeActiveOrders(): Flow<List<FuelOrder>> =
        orderDao.observeActiveOrders().map { list -> list.map { it.toDomain() } }

    fun observeActiveOrdersForFueler(fuelerId: String): Flow<List<FuelOrder>> =
        orderDao.observeActiveOrdersForFueler(fuelerId)
            .map { list -> list.map { it.toDomain() } }

    fun observeOrder(orderId: String): Flow<FuelOrder?> =
        orderDao.observeOrder(orderId).map { it?.toDomain() }

    fun observeFuelers(): Flow<List<Fueler>> =
        fuelerDao.observeFuelers().map { list -> list.map { it.toDomain() } }

    fun observeStatusEvents(orderId: String): Flow<List<StatusEvent>> =
        eventDao.observeEventsForOrder(orderId).map { list -> list.map { it.toDomain() } }

    /**
     * Pulls the latest dispatch data and merges it into Room.
     * Existing rows are untouched (OnConflictStrategy.IGNORE), so local
     * status changes survive a refresh. Returns failure on network errors
     * so the UI can show a message while keeping cached data on screen.
     */
    suspend fun refreshFromRemote(): Result<Unit> = runCatching {
        val response = api.getDispatchData()
        val syncTime = Instant.now()

        val existingIds = orderDao.getAllIds().toSet()

        fuelerDao.insertAll(response.fuelers.map { it.toEntity() })
        orderDao.insertAll(response.orders.map { it.toEntity(syncTime) })

        // Log an initial status event only for orders we have not seen before.
        response.orders
            .filter { it.id !in existingIds }
            .forEach { dto ->
                eventDao.insert(
                    StatusEventEntity(
                        orderId = dto.id,
                        fromStatus = null,
                        toStatus = dto.status.let { raw ->
                            OrderStatus.entries.firstOrNull { it.name == raw }
                                ?: OrderStatus.PENDING
                        },
                        timestampMillis = syncTime.toEpochMilli()
                    )
                )
            }
    }

    suspend fun assignFueler(orderId: String, fuelerId: String, currentStatus: OrderStatus) {
        orderDao.assignFueler(orderId, fuelerId)
        logEvent(orderId, currentStatus, OrderStatus.ASSIGNED)
    }

    suspend fun unassignFueler(orderId: String, currentStatus: OrderStatus) {
        orderDao.unassignFueler(orderId)
        logEvent(orderId, currentStatus, OrderStatus.PENDING)
    }

    suspend fun startFueling(orderId: String, currentStatus: OrderStatus) {
        orderDao.updateStatus(orderId, OrderStatus.IN_PROGRESS.name)
        logEvent(orderId, currentStatus, OrderStatus.IN_PROGRESS)
    }

    suspend fun completeOrder(orderId: String, actualLbs: Int, currentStatus: OrderStatus) {
        orderDao.completeOrder(orderId, actualLbs)
        logEvent(orderId, currentStatus, OrderStatus.COMPLETED)
    }

    private suspend fun logEvent(orderId: String, from: OrderStatus, to: OrderStatus) {
        eventDao.insert(
            StatusEventEntity(
                orderId = orderId,
                fromStatus = from,
                toStatus = to,
                timestampMillis = Instant.now().toEpochMilli()
            )
        )
    }
}