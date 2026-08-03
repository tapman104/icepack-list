package com.yourname.icepacklist.feature.search.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.MultiSearchResult
import retrofit2.HttpException
import java.io.IOException

import com.yourname.icepacklist.core.datastore.ContentFilter

class SearchPagingSource(
    private val apiService: TmdbApiService,
    private val query: String,
    private val filter: ContentFilter? = null
) : PagingSource<Int, MultiSearchResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MultiSearchResult> {
        if (query.isBlank()) {
            return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
        }
        val position = params.key ?: 1
        return try {
            val response = apiService.searchMulti(query, position)
            var results = response.results
            
            if (filter != null && filter != ContentFilter.ALL && filter.originCountry != null) {
                results = results.filter { item ->
                    item.mediaType == "person" || (item.originCountry?.contains(filter.originCountry) == true)
                }
            }
            
            LoadResult.Page(
                data = results,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (results.isEmpty() || position >= response.totalPages) null else position + 1
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MultiSearchResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
