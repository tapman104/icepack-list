package com.yourname.icepacklist.feature.watchlist.data

import com.yourname.icepacklist.core.database.WatchlistDao
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.core.database.WatchStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepository @Inject constructor(private val dao: WatchlistDao) {
    fun getAll(): Flow<List<WatchlistEntity>> = dao.getAll()
    fun getByStatus(status: WatchStatus): Flow<List<WatchlistEntity>> = dao.getByStatus(status)
    suspend fun add(item: WatchlistEntity) = dao.insert(item)
    suspend fun remove(id: Int, mediaType: MediaType) = dao.delete(WatchlistEntity(id, mediaType, "", null, 0.0, null, WatchStatus.WATCHING))
    suspend fun getItem(id: Int, mediaType: MediaType) = dao.getItem(id, mediaType)
    suspend fun updateStatus(id: Int, mediaType: MediaType, status: WatchStatus) = dao.updateStatus(id, mediaType, status)
}
