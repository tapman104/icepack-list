package com.yourname.icepacklist.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourname.icepacklist.core.database.entity.SearchCacheEntity

@Dao
interface SearchCacheDao {
    @Query("SELECT * FROM search_cache WHERE `query` = :query AND page = :page")
    suspend fun get(query: String, page: Int): SearchCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SearchCacheEntity)

    @Query("DELETE FROM search_cache WHERE fetchedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
