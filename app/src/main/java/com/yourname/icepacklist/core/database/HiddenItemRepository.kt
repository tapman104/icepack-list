package com.yourname.icepacklist.core.database

import com.yourname.icepacklist.core.database.dao.HiddenItemDao
import com.yourname.icepacklist.core.database.entity.HiddenItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HiddenItemRepository @Inject constructor(
    private val dao: HiddenItemDao
) {
    suspend fun hide(id: Int, mediaType: String, title: String) = dao.hide(HiddenItemEntity(id, mediaType, title))
    suspend fun unhide(id: Int, mediaType: String) = dao.unhide(id, mediaType)
    fun getAllHiddenIds(): Flow<List<Int>> = dao.getAllHiddenIds()
    fun getAll(): Flow<List<HiddenItemEntity>> = dao.getAll()
}
