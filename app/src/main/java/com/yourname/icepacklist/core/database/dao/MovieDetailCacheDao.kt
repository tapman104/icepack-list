package com.yourname.icepacklist.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourname.icepacklist.core.database.entity.MovieDetailCacheEntity

@Dao
interface MovieDetailCacheDao {
    @Query("SELECT * FROM movie_detail_cache WHERE movieId = :id")
    suspend fun get(id: Int): MovieDetailCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MovieDetailCacheEntity)

    @Query("DELETE FROM movie_detail_cache WHERE fetchedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
