package com.yourname.icepacklist.feature.search.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.MultiSearchResult
import retrofit2.HttpException
import java.io.IOException
import com.yourname.icepacklist.core.datastore.ContentFilter
import com.yourname.icepacklist.core.datastore.isAll
import com.yourname.icepacklist.core.datastore.originCountryParam
import com.yourname.icepacklist.core.datastore.withGenresParam
import com.yourname.icepacklist.core.datastore.withoutGenresParam
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
class SearchPagingSource(
    private val apiService: TmdbApiService,
    private val query: String,
    private val filter: Set<ContentFilter> = setOf(ContentFilter.ALL)
) : PagingSource<Int, MultiSearchResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MultiSearchResult> {
        if (query.isBlank()) {
            return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
        }
        val position = params.key ?: 1
        return try {
            val useDiscover = filter.isNotEmpty() && !filter.isAll()
            
            if (!useDiscover) {
                val response = apiService.searchMulti(query, position)
                LoadResult.Page(
                    data = response.results,
                    prevKey = if (position == 1) null else position - 1,
                    nextKey = if (response.results.isEmpty() || position >= response.totalPages) null else position + 1
                )
            } else {
                val originCountry = filter.originCountryParam()
                val withGenres = filter.withGenresParam()
                val withoutGenres = filter.withoutGenresParam()
                
                val (movies, tvs) = coroutineScope {
                    val m = async { apiService.discoverMovie(originCountry = originCountry, withGenres = withGenres, withoutGenres = withoutGenres, page = position) }
                    val t = async { apiService.discoverTv(originCountry = originCountry, withGenres = withGenres, withoutGenres = withoutGenres, page = position) }
                    Pair(m.await(), t.await())
                }
                
                val mergedResults = mutableListOf<MultiSearchResult>()
                val movieIter = movies.results.iterator()
                val tvIter = tvs.results.iterator()
                
                while (movieIter.hasNext() || tvIter.hasNext()) {
                    if (movieIter.hasNext()) {
                        mergedResults.add(MultiSearchResult.fromMovie(movieIter.next()))
                    }
                    if (tvIter.hasNext()) {
                        mergedResults.add(MultiSearchResult.fromTv(tvIter.next()))
                    }
                }
                
                val maxPages = maxOf(movies.totalPages, tvs.totalPages)
                
                LoadResult.Page(
                    data = mergedResults,
                    prevKey = if (position == 1) null else position - 1,
                    nextKey = if (mergedResults.isEmpty() || position >= maxPages) null else position + 1
                )
            }
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
