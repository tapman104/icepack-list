package com.yourname.icepacklist.feature.watchlist.data

import com.yourname.icepacklist.core.database.WatchlistDao
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.database.MediaType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Singleton
class WatchlistRepository @Inject constructor(private val dao: WatchlistDao) {
    fun getAll(): Flow<List<WatchlistEntity>> = dao.getAll()
    fun getByStatus(status: String): Flow<List<WatchlistEntity>> = dao.getByStatus(status)
    suspend fun add(item: WatchlistEntity) = dao.insert(item)
    suspend fun update(item: WatchlistEntity) = dao.update(item)
    suspend fun remove(id: Int, mediaType: MediaType) = dao.delete(WatchlistEntity(id, mediaType, "", null, 0.0, null, "WATCHING"))
    suspend fun getItem(id: Int, mediaType: MediaType) = dao.getItem(id, mediaType)
    fun getEntry(id: Int, mediaType: MediaType) = dao.getEntry(id, mediaType)
    suspend fun updateStatus(id: Int, mediaType: MediaType, status: String) = dao.updateStatus(id, mediaType, status)

    suspend fun exportToJson(moshi: Moshi): String {
        val list = dao.getAll().first()
        val listType = Types.newParameterizedType(List::class.java, WatchlistEntity::class.java)
        val adapter = moshi.adapter<List<WatchlistEntity>>(listType)
        return adapter.toJson(list)
    }

    suspend fun importFromJson(moshi: Moshi, json: String): Int {
        return try {
            val listType = Types.newParameterizedType(List::class.java, WatchlistEntity::class.java)
            val adapter = moshi.adapter<List<WatchlistEntity>>(listType)
            val list = adapter.fromJson(json)
            var importedCount = 0
            list?.forEach { entity ->
                // Merge strategy: update if exists, insert if new. 
                // Our DAO insert uses REPLACE strategy, so it functions as an upsert.
                dao.insert(entity)
                importedCount++
            }
            importedCount
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
}
