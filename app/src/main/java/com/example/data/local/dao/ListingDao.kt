package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id")
    fun getListingById(id: String): Flow<ListingEntity?>

    @Query("SELECT * FROM listings WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getListingsByCategory(categoryId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE wilayaCode = :wilayaCode ORDER BY createdAt DESC")
    fun getListingsByWilaya(wilayaCode: Int): Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllListings(listings: List<ListingEntity>)

    @Update
    suspend fun updateListing(listing: ListingEntity)

    @Query("UPDATE listings SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFav: Boolean)

    @Query("UPDATE listings SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementViews(id: String)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListingById(id: String)

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun getCount(): Int
}
