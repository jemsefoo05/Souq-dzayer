package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.ListingDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ListingEntity
import com.example.data.local.entity.OrderEntity

@Database(
    entities = [ListingEntity::class, OrderEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun listingDao(): ListingDao
    abstract fun orderDao(): OrderDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "souqdz_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
