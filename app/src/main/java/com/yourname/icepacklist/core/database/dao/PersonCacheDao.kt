package com.yourname.icepacklist.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourname.icepacklist.core.database.entity.PersonCacheEntity

@Dao
interface PersonCacheDao {
    @Query("SELECT * FROM person_cache WHERE personId = :id")
    suspend fun get(id: Int): PersonCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PersonCacheEntity)

    @Query("DELETE FROM person_cache WHERE fetchedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
