package com.example.data.repository

import com.example.data.local.TShirtOrderDao
import com.example.data.model.OrderDataSheetSummary
import com.example.data.model.OrderStatus
import com.example.data.model.SmsStatus
import com.example.data.model.SyncStatus
import com.example.data.model.TShirtOrder
import com.example.network.NetworkMonitor
import com.example.sync.FirebaseSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderRepository(
    private val orderDao: TShirtOrderDao,
    private val syncManager: FirebaseSyncManager,
    private val networkMonitor: NetworkMonitor,
    private val appScope: CoroutineScope
) {
    val allOrders: Flow<List<TShirtOrder>> = orderDao.getAllOrders()

    init {
        // Automatically attach continuous cloud sync with Firebase Realtime Database
        // Cloud is the sovereign Source of Truth. If local Room is empty (reinstall),
        // it instantly restores all orders from 'orders/'.
        syncManager.startRealtimeCloudSync(appScope)

        // Automatically sync pending offline orders and pull updates when connectivity is restored
        appScope.launch {
            networkMonitor.onInternetRestored.collect {
                syncManager.pullOrdersFromCloudOnce()
                syncManager.syncPendingOrders()
            }
        }
    }

    suspend fun restoreFromCloud(): Int = withContext(Dispatchers.IO) {
        syncManager.pullOrdersFromCloudOnce()
    }

    fun searchOrders(query: String): Flow<List<TShirtOrder>> {
        return if (query.isBlank()) {
            orderDao.getAllOrders()
        } else {
            orderDao.searchOrders(query.trim())
        }
    }

    fun getOrdersByStatus(status: OrderStatus): Flow<List<TShirtOrder>> {
        return orderDao.getOrdersByStatus(status)
    }

    fun getOrderById(id: Long): Flow<TShirtOrder?> {
        return orderDao.getOrderById(id)
    }

    suspend fun getOrderByIdDirect(id: Long): TShirtOrder? = withContext(Dispatchers.IO) {
        orderDao.getOrderByIdDirect(id)
    }

    suspend fun placeOrder(
        customerName: String,
        mobileNumber: String,
        unitPrice: Double,
        qtyChild12: Int,
        qtyS: Int,
        qtyM: Int,
        qtyL: Int,
        qtyXL: Int,
        qtyXXL: Int,
        qtyXXXL: Int,
        trackingBaseUrl: String = "https://podderpara.shop",
        note: String = ""
    ): TShirtOrder = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        var generatedId = TShirtOrder.generateOrderId()

        // Ensure uniqueness in database
        while (orderDao.getOrderByOrderId(generatedId) != null) {
            generatedId = TShirtOrder.generateOrderId()
        }

        val cleanBaseUrl = trackingBaseUrl.trim().removeSuffix("/")
        val trackingUrl = when {
            cleanBaseUrl.contains("?id=") || cleanBaseUrl.endsWith("?id") -> "$cleanBaseUrl$generatedId"
            cleanBaseUrl.contains("?") -> "$cleanBaseUrl&id=$generatedId"
            cleanBaseUrl.endsWith("/tracker") -> "$cleanBaseUrl/?id=$generatedId"
            else -> "$cleanBaseUrl/?id=$generatedId"
        }

        val isReachable = networkMonitor.checkRealReachability()

        val newOrder = TShirtOrder(
            id = 0,
            orderId = generatedId,
            customerName = customerName.trim(),
            mobileNumber = mobileNumber.trim(),
            unitPrice = unitPrice,
            qtyChild12 = qtyChild12,
            qtyS = qtyS,
            qtyM = qtyM,
            qtyL = qtyL,
            qtyXL = qtyXL,
            qtyXXL = qtyXXL,
            qtyXXXL = qtyXXXL,
            trackingUrl = trackingUrl,
            status = OrderStatus.UNDER_PROCESSING,
            syncStatus = if (isReachable) SyncStatus.PENDING_SYNC else SyncStatus.LOCAL_ONLY,
            smsStatus = SmsStatus.NOT_SENT,
            createdAt = now,
            updatedAt = now,
            note = note
        )

        // 1. Save to Room database first (Local First)
        val insertedId = orderDao.insertOrder(newOrder)
        val savedOrder = newOrder.copy(id = insertedId)

        // 2. If real internet is available, push immediately to Firebase
        if (isReachable) {
            appScope.launch {
                syncManager.syncSingleOrder(savedOrder)
            }
        }

        return@withContext savedOrder
    }

    suspend fun updateOrder(order: TShirtOrder) = withContext(Dispatchers.IO) {
        val updated = order.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_SYNC
        )
        orderDao.updateOrder(updated)

        if (networkMonitor.isOnline.value) {
            appScope.launch {
                syncManager.syncSingleOrder(updated)
            }
        }
    }

    suspend fun updateOrderStatus(id: Long, newStatus: OrderStatus) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        orderDao.updateOrderStatus(id, newStatus, now)
        orderDao.updateSyncStatus(id, SyncStatus.PENDING_SYNC, null, now)

        if (networkMonitor.isOnline.value) {
            val order = orderDao.getOrderByIdDirect(id)
            if (order != null) {
                appScope.launch {
                    syncManager.syncSingleOrder(order)
                }
            }
        }
    }

    suspend fun updateSmsStatus(
        orderId: Long,
        smsStatus: SmsStatus,
        errorMessage: String? = null,
        sentAt: Long? = null
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        orderDao.updateSmsStatus(
            id = orderId,
            smsStatus = smsStatus,
            errorMessage = errorMessage,
            sentAt = sentAt,
            updatedAt = now
        )

        // Sync updated SMS status to Firebase as well
        val order = orderDao.getOrderByIdDirect(orderId)
        if (order != null && networkMonitor.isOnline.value) {
            appScope.launch {
                syncManager.syncSingleOrder(order)
            }
        }
    }

    suspend fun deleteOrder(order: TShirtOrder) = withContext(Dispatchers.IO) {
        orderDao.deleteOrder(order)
        if (networkMonitor.isOnline.value) {
            appScope.launch {
                syncManager.deleteOrderFromFirebase(order.orderId)
            }
        }
    }

    suspend fun deleteOrderById(id: Long) = withContext(Dispatchers.IO) {
        val order = orderDao.getOrderByIdDirect(id)
        orderDao.deleteOrderById(id)
        if (order != null && networkMonitor.isOnline.value) {
            appScope.launch {
                syncManager.deleteOrderFromFirebase(order.orderId)
            }
        }
    }

    suspend fun syncNow() = withContext(Dispatchers.IO) {
        syncManager.syncPendingOrders()
    }

    fun calculateDataSheetSummary(orders: List<TShirtOrder>): OrderDataSheetSummary {
        var totalTShirts = 0
        var grandTotalAmount = 0.0

        var totalChild12 = 0
        var totalS = 0
        var totalM = 0
        var totalL = 0
        var totalXL = 0
        var totalXXL = 0
        var totalXXXL = 0

        var underProcessingCount = 0
        var underProcessingAmount = 0.0

        var paidCount = 0
        var paidAmount = 0.0

        var deliveredCount = 0
        var deliveredAmount = 0.0

        var cancelledCount = 0
        var cancelledAmount = 0.0

        var smsPendingCount = 0
        var smsSentCount = 0
        var smsDeliveredCount = 0
        var smsFailedCount = 0
        var cloudSyncedCount = 0
        var cloudPendingCount = 0

        for (order in orders) {
            totalChild12 += order.qtyChild12
            totalS += order.qtyS
            totalM += order.qtyM
            totalL += order.qtyL
            totalXL += order.qtyXL
            totalXXL += order.qtyXXL
            totalXXXL += order.qtyXXXL

            val orderQty = order.totalQuantity
            val orderAmount = order.totalAmount

            if (order.status != OrderStatus.CANCELLED) {
                totalTShirts += orderQty
                grandTotalAmount += orderAmount
            }

            when (order.status) {
                OrderStatus.UNDER_PROCESSING -> {
                    underProcessingCount++
                    underProcessingAmount += orderAmount
                }
                OrderStatus.PAID -> {
                    paidCount++
                    paidAmount += orderAmount
                }
                OrderStatus.DELIVERED -> {
                    deliveredCount++
                    deliveredAmount += orderAmount
                }
                OrderStatus.CANCELLED -> {
                    cancelledCount++
                    cancelledAmount += orderAmount
                }
            }

            when (order.smsStatus) {
                SmsStatus.NOT_SENT, SmsStatus.SENDING -> smsPendingCount++
                SmsStatus.SENT -> smsSentCount++
                SmsStatus.DELIVERED -> smsDeliveredCount++
                SmsStatus.FAILED -> smsFailedCount++
            }

            when (order.syncStatus) {
                SyncStatus.SYNCED -> cloudSyncedCount++
                else -> cloudPendingCount++
            }
        }

        return OrderDataSheetSummary(
            totalOrders = orders.size,
            totalTShirts = totalTShirts,
            grandTotalAmount = grandTotalAmount,
            totalChild12 = totalChild12,
            totalS = totalS,
            totalM = totalM,
            totalL = totalL,
            totalXL = totalXL,
            totalXXL = totalXXL,
            totalXXXL = totalXXXL,
            underProcessingCount = underProcessingCount,
            underProcessingAmount = underProcessingAmount,
            paidCount = paidCount,
            paidAmount = paidAmount,
            deliveredCount = deliveredCount,
            deliveredAmount = deliveredAmount,
            cancelledCount = cancelledCount,
            cancelledAmount = cancelledAmount,
            smsPendingCount = smsPendingCount,
            smsSentCount = smsSentCount,
            smsDeliveredCount = smsDeliveredCount,
            smsFailedCount = smsFailedCount,
            cloudSyncedCount = cloudSyncedCount,
            cloudPendingCount = cloudPendingCount
        )
    }
}
