package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceDzd: Long,
    val isNegotiable: Boolean,
    val categoryId: String,
    val subcategory: String,
    val condition: String, // "Neuf", "Comme neuf", "Bon état", "Occasion"
    val wilayaCode: Int,
    val wilayaName: String,
    val communeName: String,
    val sellerName: String,
    val sellerPhone: String,
    val sellerRating: Float,
    val sellerVerified: Boolean,
    val sellerBaridiRip: String,
    val imageUrl: String, // Resource name or URL or placeholder identifier
    val deliverySupported: Boolean,
    val deliveryCarriersJson: String, // JSON list of allowed carriers e.g. "Yalidine,ZR Express"
    val deliveryHomeFee: Long,
    val deliveryDeskFee: Long,
    val isUrgent: Boolean,
    val isFeatured: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val viewsCount: Int = 0,
    val isFavorite: Boolean = false
)
