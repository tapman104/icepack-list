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
import com.yourname.icepacklist.core.database.dao.MovieDetailCacheDao
import com.yourname.icepacklist.core.database.dao.TvDetailCacheDao
import com.yourname.icepacklist.core.database.entity.MovieDetailCacheEntity
import com.yourname.icepacklist.core.database.entity.TvDetailCacheEntity
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

@Singleton
class WatchlistRepository @Inject constructor(
    private val dao: WatchlistDao,
    private val tmdbApi: TmdbApiService,
    private val movieCacheDao: MovieDetailCacheDao,
    private val tvCacheDao: TvDetailCacheDao,
    private val moshi: Moshi
) {
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

    fun importFromJson(json: String): Flow<ImportState> = flow {
        try {
            val jsonArray = org.json.JSONArray(json)
            val total = jsonArray.length()
            var importedCount = 0
            var skippedCount = 0

            for (i in 0 until total) {
                try {
                    val itemObj = jsonArray.optJSONObject(i)
                    if (itemObj == null) {
                        skippedCount++
                        emit(ImportState.Progress(i + 1, total))
                        continue
                    }

                    val id = if (itemObj.has("id")) itemObj.optInt("id", -1) else -1
                    val mediaTypeStr = itemObj.optString("mediaType", "")
                    
                    if (id == -1 || mediaTypeStr.isEmpty()) {
                        skippedCount++
                        emit(ImportState.Progress(i + 1, total))
                        continue
                    }
                    
                    val mediaType = try {
                        MediaType.valueOf(mediaTypeStr)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                    
                    if (mediaType == null) {
                        skippedCount++
                        emit(ImportState.Progress(i + 1, total))
                        continue
                    }

                    if (mediaType == MediaType.MOVIE) {
                        val detail = tmdbApi.getMovieDetails(id)
                        val adapter = moshi.adapter(MovieDetail::class.java)
                        val detailJson = adapter.toJson(detail)
                        movieCacheDao.upsert(MovieDetailCacheEntity(id, detailJson, System.currentTimeMillis()))
                    } else if (mediaType == MediaType.TV) {
                        val detail = tmdbApi.getTvShowDetails(id)
                        val adapter = moshi.adapter(TvShowDetail::class.java)
                        val detailJson = adapter.toJson(detail)
                        tvCacheDao.upsert(TvDetailCacheEntity(id, detailJson, System.currentTimeMillis()))
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
                } catch (e: Exception) {
                    skippedCount++
                }
                emit(ImportState.Progress(i + 1, total))
            }
            emit(ImportState.Success(importedCount, skippedCount))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ImportState.Error(e.message))
        }
    }.flowOn(Dispatchers.IO)
}
