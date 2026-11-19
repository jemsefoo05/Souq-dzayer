package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE listingId = :listingId ORDER BY timestamp ASC")
    fun getMessagesForListing(listingId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET offerStatus = :status WHERE id = :messageId")
    suspend fun updateOfferStatus(messageId: Long, status: String)

    @Query("DELETE FROM chat_messages WHERE listingId = :listingId")
    suspend fun deleteMessagesForListing(listingId: String)
}
