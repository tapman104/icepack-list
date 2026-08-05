package com.yourname.icepacklist.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistEntity)

    @Update
    suspend fun update(item: WatchlistEntity)

    @Delete
    suspend fun delete(item: WatchlistEntity)

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAllByDateAdded(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist ORDER BY title ASC")
    fun getAllByTitleAz(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist ORDER BY voteAverage DESC")
    fun getAllByRating(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist ORDER BY year DESC")
    fun getAllByYear(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC LIMIT 1")
    suspend fun getLatestWatchlistItem(): WatchlistEntity?

    @Query("SELECT * FROM watchlist WHERE status = :status ORDER BY addedAt DESC")
    fun getByStatusByDateAdded(status: String): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE status = :status ORDER BY title ASC")
    fun getByStatusByTitleAz(status: String): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE status = :status ORDER BY voteAverage DESC")
    fun getByStatusByRating(status: String): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE status = :status ORDER BY year DESC")
    fun getByStatusByYear(status: String): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE id = :id AND mediaType = :mediaType LIMIT 1")
    suspend fun getItem(id: Int, mediaType: MediaType): WatchlistEntity?

    @Query("SELECT * FROM watchlist WHERE id = :id AND mediaType = :mediaType")
    fun getEntry(id: Int, mediaType: MediaType): Flow<WatchlistEntity?>

    @Query("UPDATE watchlist SET status = :status WHERE id = :id AND mediaType = :mediaType")
    suspend fun updateStatus(id: Int, mediaType: MediaType, status: String)
}
