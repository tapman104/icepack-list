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

    suspend fun importFromJson(json: String): Pair<Int, Int> {
        return try {
            val jsonArray = org.json.JSONArray(json)
            var importedCount = 0
            var skippedCount = 0

            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.optJSONObject(i)
                if (itemObj == null) {
                    skippedCount++
                    continue
                }

                val id = if (itemObj.has("id")) itemObj.optInt("id", -1) else -1
                val mediaTypeStr = itemObj.optString("mediaType", "")
                
                if (id == -1 || mediaTypeStr.isEmpty()) {
                    skippedCount++
                    continue
                }
                
                val mediaType = try {
                    MediaType.valueOf(mediaTypeStr)
                } catch (e: IllegalArgumentException) {
                    null
                }
                
                if (mediaType == null) {
                    skippedCount++
                    continue
                }
                
                val title = itemObj.optString("title", "")
                val voteAverage = itemObj.optDouble("voteAverage", 0.0)
                val posterPath = itemObj.optString("posterPath").ifEmpty {
                    itemObj.optString("poster_path")
                }.ifEmpty { null }
                val year = if (itemObj.isNull("year")) null else itemObj.optString("year")
                val status = itemObj.optString("status", "PLAN_TO_WATCH")
                val rating = if (itemObj.isNull("rating")) null else itemObj.optDouble("rating").toFloat()
                val startDate = if (itemObj.isNull("startDate")) null else itemObj.optString("startDate")
                val finishDate = if (itemObj.isNull("finishDate")) null else itemObj.optString("finishDate")
                val notes = if (itemObj.isNull("notes")) null else itemObj.optString("notes")
                val episodesWatched = if (itemObj.isNull("episodesWatched")) null else itemObj.optInt("episodesWatched")
                val addedAt = itemObj.optLong("addedAt", System.currentTimeMillis())
                
                val entity = WatchlistEntity(
                    id = id,
                    mediaType = mediaType,
                    title = title,
                    posterPath = posterPath,
                    voteAverage = voteAverage,
                    year = year,
                    status = status,
                    rating = rating,
                    startDate = startDate,
                    finishDate = finishDate,
                    notes = notes,
                    episodesWatched = episodesWatched,
                    addedAt = addedAt
                )
                
                dao.insert(entity)
                importedCount++
            }
            Pair(importedCount, skippedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(-1, 0)
        }
    }
}
