package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.OrderEntity
import com.example.data.model.DeliveryStatus
import com.example.data.model.EscrowStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE trackingNumber = :trackingNumber")
    fun getOrderByTracking(trackingNumber: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET deliveryStatus = :status, checkpointsJson = :checkpointsJson WHERE orderId = :orderId")
    suspend fun updateDeliveryStatus(orderId: String, status: DeliveryStatus, checkpointsJson: String)

    @Query("UPDATE orders SET escrowStatus = :escrowStatus WHERE orderId = :orderId")
    suspend fun updateEscrowStatus(orderId: String, escrowStatus: EscrowStatus)

    @Query("DELETE FROM orders WHERE orderId = :orderId")
    suspend fun deleteOrder(orderId: String)
}
