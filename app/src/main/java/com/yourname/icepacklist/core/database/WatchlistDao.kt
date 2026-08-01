package com.yourname.icepacklist.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistEntity)

    @Delete
    suspend fun delete(item: WatchlistEntity)

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE status = :status ORDER BY addedAt DESC")
    fun getByStatus(status: WatchStatus): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE id = :id AND mediaType = :mediaType LIMIT 1")
    suspend fun getItem(id: Int, mediaType: MediaType): WatchlistEntity?

    @Query("UPDATE watchlist SET status = :status WHERE id = :id AND mediaType = :mediaType")
    suspend fun updateStatus(id: Int, mediaType: MediaType, status: WatchStatus)
}
