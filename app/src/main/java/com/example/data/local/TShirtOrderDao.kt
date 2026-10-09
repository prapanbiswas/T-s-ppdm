package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OrderStatus
import com.example.data.model.SmsStatus
import com.example.data.model.SyncStatus
import com.example.data.model.TShirtOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TShirtOrderDao {

    @Query("SELECT * FROM tshirt_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<TShirtOrder>>

    @Query("""
        SELECT * FROM tshirt_orders 
        WHERE customerName LIKE '%' || :query || '%' 
           OR mobileNumber LIKE '%' || :query || '%'
           OR orderId LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchOrders(query: String): Flow<List<TShirtOrder>>

    @Query("SELECT * FROM tshirt_orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<TShirtOrder>>

    @Query("SELECT * FROM tshirt_orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: Long): Flow<TShirtOrder?>

    @Query("SELECT * FROM tshirt_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderByIdDirect(id: Long): TShirtOrder?

    @Query("SELECT * FROM tshirt_orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderByOrderId(orderId: String): TShirtOrder?

    @Query("SELECT * FROM tshirt_orders WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncOrders(): List<TShirtOrder>

    @Query("SELECT * FROM tshirt_orders WHERE smsStatus = 'FAILED'")
    suspend fun getFailedSmsOrders(): List<TShirtOrder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: TShirtOrder): Long

    @Update
    suspend fun updateOrder(order: TShirtOrder)

    @Delete
    suspend fun deleteOrder(order: TShirtOrder)

    @Query("DELETE FROM tshirt_orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)

    @Query("UPDATE tshirt_orders SET status = :newStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOrderStatus(id: Long, newStatus: OrderStatus, updatedAt: Long)

    @Query("UPDATE tshirt_orders SET syncStatus = :syncStatus, firebaseId = :firebaseId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, syncStatus: SyncStatus, firebaseId: String?, updatedAt: Long)

    @Query("UPDATE tshirt_orders SET smsStatus = :smsStatus, smsErrorMessage = :errorMessage, smsSentAt = :sentAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSmsStatus(id: Long, smsStatus: SmsStatus, errorMessage: String?, sentAt: Long?, updatedAt: Long)

    suspend fun upsertByOrderId(order: TShirtOrder): Long {
        val existing = getOrderByOrderId(order.orderId)
        return if (existing != null) {
            updateOrder(order.copy(id = existing.id))
            existing.id
        } else {
            insertOrder(order.copy(id = 0))
        }
    }
}
