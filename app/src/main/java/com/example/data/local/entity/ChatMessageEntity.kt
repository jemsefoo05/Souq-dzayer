package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listingId: String,
    val senderId: String, // "buyer" or "seller"
    val senderName: String,
    val messageText: String,
    val isOffer: Boolean = false,
    val offerAmountDzd: Long? = null,
    val offerStatus: String? = null, // "PENDING", "ACCEPTED", "DECLINED"
    val timestamp: Long = System.currentTimeMillis()
)
