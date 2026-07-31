package com.yourname.icepacklist.feature.detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.detail.data.DetailRepository
import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.feature.home.domain.VideoResult
import com.yourname.icepacklist.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Error(val message: String) : DetailUiState
    data class Success(
        val movie: MovieDetail,
        val credits: CreditsResponse,
        val videos: List<VideoResult>,
        val similar: List<Movie>
    ) : DetailUiState
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val apiService: TmdbApiService,
    private val repository: DetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle[Routes.Detail.ARG_MOVIE_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadMovieDetail()
    }

    fun loadMovieDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val movieDef = async { apiService.getMovieDetails(movieId) }
                val creditsDef = async { apiService.getMovieCredits(movieId) }
                val videosDef = async { apiService.getMovieVideos(movieId) }
                val similarDef = async { apiService.getSimilarMovies(movieId) }

                awaitAll(movieDef, creditsDef, videosDef, similarDef)

                val movie = movieDef.await()
                val credits = creditsDef.await()
                val videosResponse = videosDef.await()
                val similarResponse = similarDef.await()

                val director = credits.crew.firstOrNull { it.job == "Director" }?.name ?: ""
                val videoResults = videosResponse.results
                    .filter { it.type == "Trailer" && it.site == "YouTube" }
                    .map { VideoResult(key = it.key, name = it.name, site = it.site, type = it.type) }
                val similarMovies = similarResponse.results.take(12)

                val updatedMovie = movie.copy(
                    director = director,
                    videos = videoResults,
                    similar = similarMovies
                )

                _uiState.value = DetailUiState.Success(
                    movie = updatedMovie,
                    credits = credits,
                    videos = videoResults,
                    similar = similarMovies
                )
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
