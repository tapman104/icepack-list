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

enum class SortOrder { DATE_ADDED, TITLE_AZ, RATING, YEAR }

@Singleton
class WatchlistRepository @Inject constructor(
    private val dao: WatchlistDao,
    private val tmdbApi: TmdbApiService,
    private val movieCacheDao: MovieDetailCacheDao,
    private val tvCacheDao: TvDetailCacheDao,
    private val moshi: Moshi
) {
    fun getAll(sortOrder: SortOrder = SortOrder.DATE_ADDED): Flow<List<WatchlistEntity>> = when (sortOrder) {
        SortOrder.DATE_ADDED -> dao.getAllByDateAdded()
        SortOrder.TITLE_AZ -> dao.getAllByTitleAz()
        SortOrder.RATING -> dao.getAllByRating()
        SortOrder.YEAR -> dao.getAllByYear()
    }
    
    fun getByStatus(status: String, sortOrder: SortOrder = SortOrder.DATE_ADDED): Flow<List<WatchlistEntity>> = when (sortOrder) {
        SortOrder.DATE_ADDED -> dao.getByStatusByDateAdded(status)
        SortOrder.TITLE_AZ -> dao.getByStatusByTitleAz(status)
        SortOrder.RATING -> dao.getByStatusByRating(status)
        SortOrder.YEAR -> dao.getByStatusByYear(status)
    }
    suspend fun add(item: WatchlistEntity) = dao.insert(item)
    suspend fun update(item: WatchlistEntity) = dao.update(item)
    suspend fun remove(id: Int, mediaType: MediaType) = dao.delete(WatchlistEntity(id, mediaType, "", null, 0.0, null, "WATCHING"))
    suspend fun getItem(id: Int, mediaType: MediaType) = dao.getItem(id, mediaType)
    fun getEntry(id: Int, mediaType: MediaType) = dao.getEntry(id, mediaType)
    suspend fun updateStatus(id: Int, mediaType: MediaType, status: String) = dao.updateStatus(id, mediaType, status)


}
