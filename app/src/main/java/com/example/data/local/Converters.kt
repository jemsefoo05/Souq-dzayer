package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DeliveryCarrier
import com.example.data.model.DeliveryServiceType
import com.example.data.model.DeliveryStatus
import com.example.data.model.EscrowStatus
import com.example.data.model.PaymentType

class Converters {
    @TypeConverter
    fun fromPaymentType(value: PaymentType): String = value.name

    @TypeConverter
    fun toPaymentType(value: String): PaymentType = try {
        PaymentType.valueOf(value)
    } catch (e: Exception) {
        PaymentType.BARIDIMOB
    }

    @TypeConverter
    fun fromEscrowStatus(value: EscrowStatus): String = value.name

    @TypeConverter
    fun toEscrowStatus(value: String): EscrowStatus = try {
        EscrowStatus.valueOf(value)
    } catch (e: Exception) {
        EscrowStatus.HELD_IN_ESCROW
    }

    @TypeConverter
    fun fromDeliveryCarrier(value: DeliveryCarrier): String = value.name

    @TypeConverter
    fun toDeliveryCarrier(value: String): DeliveryCarrier = try {
        DeliveryCarrier.valueOf(value)
    } catch (e: Exception) {
        DeliveryCarrier.YALIDINE
    }

    @TypeConverter
    fun fromDeliveryServiceType(value: DeliveryServiceType): String = value.name

    @TypeConverter
    fun toDeliveryServiceType(value: String): DeliveryServiceType = try {
        DeliveryServiceType.valueOf(value)
    } catch (e: Exception) {
        DeliveryServiceType.HOME_DELIVERY
    }

    @TypeConverter
    fun fromDeliveryStatus(value: DeliveryStatus): String = value.name

    @TypeConverter
    fun toDeliveryStatus(value: String): DeliveryStatus = try {
        DeliveryStatus.valueOf(value)
    } catch (e: Exception) {
        DeliveryStatus.ORDER_CONFIRMED
    }
}
