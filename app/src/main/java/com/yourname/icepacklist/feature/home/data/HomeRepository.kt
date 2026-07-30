package com.yourname.icepacklist.feature.home.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.yourname.icepacklist.core.database.IcepackDatabase
import com.yourname.icepacklist.core.database.toDomain
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val database: IcepackDatabase
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getPopularMovies(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = MovieRemoteMediator(apiService, database),
            pagingSourceFactory = { database.movieDao().getPagingSource() }
        ).flow.map { pagingData -> 
            pagingData.map { it.toDomain() } 
        }
    }
}
