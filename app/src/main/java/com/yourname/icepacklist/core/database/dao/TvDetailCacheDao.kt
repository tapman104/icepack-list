package com.yourname.icepacklist.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourname.icepacklist.core.database.entity.TvDetailCacheEntity

@Dao
interface TvDetailCacheDao {
    @Query("SELECT * FROM tv_detail_cache WHERE tvId = :id")
    suspend fun get(id: Int): TvDetailCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TvDetailCacheEntity)

    @Query("DELETE FROM tv_detail_cache WHERE fetchedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
