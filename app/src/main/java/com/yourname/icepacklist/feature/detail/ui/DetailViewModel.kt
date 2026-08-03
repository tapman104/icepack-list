package com.yourname.icepacklist.feature.detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.feature.watchlist.domain.WatchlistStatus
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.detail.data.DetailRepository
import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.feature.home.domain.VideoResult
import com.yourname.icepacklist.feature.home.domain.WatchProvider
import com.yourname.icepacklist.feature.home.domain.Keyword
import com.yourname.icepacklist.feature.home.domain.Review
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
        val similar: List<Movie>,
        val watchProviders: List<WatchProvider>,
        val keywords: List<Keyword>,
        val reviews: List<Review>
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

    // H10 — single getAll() subscription; isInWatchlist and watchlistStatus are derived from it,
    // so only one database flow is active instead of two separate full-list scans per DB write
    private val watchlistEntry: StateFlow<WatchlistEntity?> = watchlistRepository.getAll()
        .map { list -> list.firstOrNull { it.id == movieId && it.mediaType == MediaType.MOVIE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isInWatchlist: StateFlow<Boolean> = watchlistEntry
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val watchlistStatus: StateFlow<String?> = watchlistEntry
        .map { it?.status }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val entryState: StateFlow<WatchlistEntity?> = watchlistRepository.getEntry(movieId, MediaType.MOVIE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadMovieDetail()
    }

    fun loadMovieDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val movieDef = async { apiService.getMovieDetails(movieId) }
                val similarDef = async { apiService.getSimilarMovies(movieId) }
                val providersDef = async { apiService.getMovieWatchProviders(movieId) }
                val keywordsDef = async { apiService.getMovieKeywords(movieId) }
                val reviewsDef = async { apiService.getMovieReviews(movieId) }

                awaitAll(movieDef, similarDef, providersDef, keywordsDef, reviewsDef)

                val movie = movieDef.await()
                val similarResponse = similarDef.await()
                val providersResponse = providersDef.await()
                val keywordsResponse = keywordsDef.await()
                val reviewsResponse = reviewsDef.await()

                val credits = movie.creditsResponse ?: CreditsResponse(id = movieId)
                val videosResponse = movie.videoResponse

                val director = credits.crew.firstOrNull { it.job == "Director" }?.name ?: ""
                val videoResults = videosResponse?.results
                    ?.filter { it.type == "Trailer" && it.site == "YouTube" }
                    ?.map { VideoResult(key = it.key, name = it.name, site = it.site, type = it.type) }
                    ?: emptyList()
                val similarMovies = similarResponse.results.distinctBy { it.id }.take(12)

                val updatedMovie = movie.copy(
                    director = director,
                    videos = videoResults,
                    similar = similarMovies
                )

                val inProviders = providersResponse.results?.get("IN")?.flatrate ?: emptyList()
                val keywords = keywordsResponse.keywords ?: keywordsResponse.results ?: emptyList()
                val reviews = reviewsResponse.results

                _uiState.value = DetailUiState.Success(
                    movie = updatedMovie,
                    credits = credits,
                    videos = videoResults,
                    similar = similarMovies,
                    watchProviders = inProviders,
                    keywords = keywords,
                    reviews = reviews
                )
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    fun addToWatchlist(status: String = WatchlistStatus.PLAN_TO_WATCH.name) {
        val currentState = _uiState.value
        if (currentState is DetailUiState.Success) {
            val movie = currentState.movie
            val entity = WatchlistEntity(
                id = movie.id,
                mediaType = MediaType.MOVIE,
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
            watchlistRepository.remove(movieId, MediaType.MOVIE)
        }
    }

    fun updateWatchlistStatus(status: String) {
        viewModelScope.launch {
            watchlistRepository.updateStatus(movieId, MediaType.MOVIE, status)
        }
    }

    fun saveEntry(updated: WatchlistEntity) {
        viewModelScope.launch {
            watchlistRepository.update(updated)
        }
    }

    fun removeEntry(entry: WatchlistEntity) {
        viewModelScope.launch {
            watchlistRepository.remove(entry.id, entry.mediaType)
        }
    }
}
