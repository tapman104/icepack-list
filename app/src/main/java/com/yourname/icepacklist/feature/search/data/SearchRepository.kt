package com.yourname.icepacklist.feature.search.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.MultiSearchResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

import com.yourname.icepacklist.core.datastore.ContentFilter

class SearchRepository @Inject constructor(
    private val apiService: TmdbApiService
) {
    fun searchMulti(query: String, filter: ContentFilter? = null): Flow<PagingData<MultiSearchResult>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { SearchPagingSource(apiService, query, filter) }
        ).flow
    }
}
