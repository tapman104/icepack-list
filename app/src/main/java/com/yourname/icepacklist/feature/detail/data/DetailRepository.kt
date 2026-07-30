package com.yourname.icepacklist.feature.detail.data

import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DetailRepository @Inject constructor(
    private val apiService: TmdbApiService
) {
    suspend fun getMovieDetails(movieId: Int): Result<MovieDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMovieDetails(movieId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
