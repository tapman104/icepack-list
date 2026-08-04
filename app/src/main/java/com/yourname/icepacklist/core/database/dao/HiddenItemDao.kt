package com.yourname.icepacklist.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourname.icepacklist.core.database.entity.HiddenItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hide(item: HiddenItemEntity)

    @Query("DELETE FROM hidden_items WHERE id = :id AND mediaType = :mediaType")
    suspend fun unhide(id: Int, mediaType: String)

    @Query("SELECT id FROM hidden_items WHERE mediaType = :mediaType")
    fun getHiddenIds(mediaType: String): Flow<List<Int>>

    @Query("SELECT id FROM hidden_items")
    fun getAllHiddenIds(): Flow<List<Int>>
    
    @Query("SELECT * FROM hidden_items ORDER BY hiddenAt DESC")
    fun getAll(): Flow<List<HiddenItemEntity>>
}
