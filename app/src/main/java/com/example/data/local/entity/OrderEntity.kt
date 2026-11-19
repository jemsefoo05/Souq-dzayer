package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DeliveryCarrier
import com.example.data.model.DeliveryServiceType
import com.example.data.model.DeliveryStatus
import com.example.data.model.EscrowStatus
import com.example.data.model.PaymentType

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val listingId: String,
    val itemTitle: String,
    val itemPriceDzd: Long,
    val deliveryFeeDzd: Long,
    val totalAmountDzd: Long,
    val itemImageUrl: String,
    val sellerName: String,
    val sellerPhone: String,
    val sellerBaridiRip: String,
    val buyerName: String,
    val buyerPhone: String,
    val buyerWilayaCode: Int,
    val buyerWilayaName: String,
    val buyerCommune: String,
    val buyerAddressLine: String,
    val paymentType: PaymentType,
    val paymentReference: String, // e.g. BaridiMob transaction ID, CIB auth code
    val escrowStatus: EscrowStatus,
    val carrier: DeliveryCarrier,
    val serviceType: DeliveryServiceType,
    val trackingNumber: String,
    val deliveryStatus: DeliveryStatus,
    val pinSecurityCode: String, // 4-digit verification code for courier inspection
    val checkpointsJson: String, // JSON formatted tracking history
    val createdAt: Long = System.currentTimeMillis(),
    val estimatedDeliveryDays: Int = 2
)
