package com.yourname.icepacklist.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourname.icepacklist.core.database.entity.HomeListCacheEntity

@Dao
interface HomeListCacheDao {
    @Query("SELECT * FROM home_list_cache WHERE cacheKey = :key")
    suspend fun get(key: String): HomeListCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HomeListCacheEntity)

    @Query("DELETE FROM home_list_cache WHERE fetchedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
