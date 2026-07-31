package com.yourname.icepacklist.feature.detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.detail.data.DetailRepository
import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.feature.home.domain.VideoResult
import com.yourname.icepacklist.feature.watchlist.data.WatchlistRepository
import com.yourname.icepacklist.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val watchlistRepository: WatchlistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle[Routes.Detail.ARG_MOVIE_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val isInWatchlist: StateFlow<Boolean> = watchlistRepository.getAll()
        .map { list -> list.any { it.id == movieId && it.mediaType == "movie" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val watchlistStatus: StateFlow<String?> = watchlistRepository.getAll()
        .map { list -> list.firstOrNull { it.id == movieId && it.mediaType == "movie" }?.status }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun addToWatchlist(status: String = "watching") {
        val currentState = _uiState.value
        if (currentState is DetailUiState.Success) {
            val movie = currentState.movie
            val entity = WatchlistEntity(
                id = movie.id,
                mediaType = "movie",
                title = movie.title,
                posterPath = movie.posterPath,
                voteAverage = movie.voteAverage,
                year = movie.releaseDate?.take(4),
                status = status
            )
            viewModelScope.launch {
                watchlistRepository.add(entity)
            }
        }
    }

    fun removeFromWatchlist() {
        viewModelScope.launch {
            watchlistRepository.remove(movieId, "movie")
        }
    }

    fun updateWatchlistStatus(status: String) {
        viewModelScope.launch {
            watchlistRepository.updateStatus(movieId, "movie", status)
        }
    }
}
