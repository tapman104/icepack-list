package com.yourname.icepacklist.feature.search.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.MultiSearchResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

import com.yourname.icepacklist.core.datastore.ContentFilter
import com.yourname.icepacklist.core.database.dao.SearchHistoryDao
import com.yourname.icepacklist.core.database.entity.SearchHistoryEntity

class SearchRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val searchHistoryDao: SearchHistoryDao
) {
    fun searchMulti(query: String, filter: Set<ContentFilter> = setOf(ContentFilter.ALL)): Flow<PagingData<MultiSearchResult>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { SearchPagingSource(apiService, query, filter) }
        ).flow
    }

    fun getSearchHistory(): Flow<List<SearchHistoryEntity>> = searchHistoryDao.getSearchHistory()

    suspend fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearchQuery(SearchHistoryEntity(query, System.currentTimeMillis()))
        }
    }

    suspend fun deleteSearchQuery(query: String) {
        searchHistoryDao.deleteSearchQuery(query)
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearAll()
    }
}
