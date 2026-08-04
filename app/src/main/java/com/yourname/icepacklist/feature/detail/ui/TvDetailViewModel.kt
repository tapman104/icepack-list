package com.yourname.icepacklist.feature.detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.database.HiddenItemRepository
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.feature.watchlist.domain.WatchlistStatus
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.detail.data.DetailRepository
import com.yourname.icepacklist.feature.home.domain.TvShow
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
import com.yourname.icepacklist.feature.home.domain.VideoResult
import com.yourname.icepacklist.feature.home.domain.WatchProvider
import com.yourname.icepacklist.feature.home.domain.Keyword
import com.yourname.icepacklist.feature.home.domain.Review
import com.yourname.icepacklist.feature.watchlist.data.WatchlistRepository
import com.yourname.icepacklist.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.Immutable
import com.yourname.icepacklist.core.datastore.matches

@Immutable
sealed interface TvDetailUiState {
    data object Loading : TvDetailUiState
    data class Error(val message: String) : TvDetailUiState
    data class Success(
        val tvShow: TvShowDetail,
        val credits: CreditsResponse,
        val videos: List<VideoResult>,
        val similar: List<TvShow>,
        val watchProviders: List<WatchProvider>,
        val keywords: List<Keyword>,
        val reviews: List<Review>
    ) : TvDetailUiState
}

@HiltViewModel
class TvDetailViewModel @Inject constructor(
    private val apiService: TmdbApiService,
    private val repository: DetailRepository,
    private val watchlistRepository: WatchlistRepository,
    private val hiddenItemRepository: HiddenItemRepository,
    private val settingsDataStore: com.yourname.icepacklist.core.datastore.ApiKeyDataStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tvId: Int = checkNotNull(savedStateHandle[Routes.TvDetail.ARG_TV_ID])

    private val _uiState = MutableStateFlow<TvDetailUiState>(TvDetailUiState.Loading)
    val uiState: StateFlow<TvDetailUiState> = _uiState.asStateFlow()

    // H11 — single getAll() subscription; isInWatchlist and watchlistStatus derived from it
    // instead of two separate full-list scans per DB write
    private val watchlistEntry: StateFlow<WatchlistEntity?> = watchlistRepository.getAll()
        .map { list -> list.firstOrNull { it.id == tvId && it.mediaType == MediaType.TV } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isInWatchlist: StateFlow<Boolean> = watchlistEntry
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val watchlistStatus: StateFlow<String?> = watchlistEntry
        .map { it?.status }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val entryState: StateFlow<WatchlistEntity?> = watchlistRepository.getEntry(tvId, MediaType.TV)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = TvDetailUiState.Loading
            try {
                coroutineScope {
                    val result = repository.getTvDetailFull(tvId)
                    val fullResponse = result.getOrThrow()

                    val tvShow = fullResponse.toTvShowDetail()
                    val credits = fullResponse.creditsResponse ?: CreditsResponse(id = tvId)
                    val videosResponse = fullResponse.videoResponse

                    val videoResults = videosResponse?.results
                        ?.filter { it.type == "Trailer" && it.site == "YouTube" }
                        ?.map { VideoResult(key = it.key, name = it.name, site = it.site, type = it.type) }
                        ?: emptyList()
                    
                    val filters = settingsDataStore.recommendationsContentFilter.first()
                    val similarShows = fullResponse.similarResponse?.results
                        ?.distinctBy { it.id }
                        ?.filter { it.matches(filters) }
                        ?.take(12) ?: emptyList()

                    val networkNames = tvShow.networksList.map { it.name }
                    val createdByStr = tvShow.createdByList.map { it.name }.joinToString(", ")

                    val updatedTvShow = tvShow.copy(
                        networks = networkNames,
                        createdBy = createdByStr,
                        videos = videoResults,
                        similar = similarShows
                    )

                    val inProviders = fullResponse.watchProvidersResponse?.results?.get("IN")?.flatrate ?: emptyList()
                    val keywords = fullResponse.keywordsResponse?.keywords ?: fullResponse.keywordsResponse?.results ?: emptyList()
                    val reviews = fullResponse.reviewsResponse?.results ?: emptyList()

                    _uiState.value = TvDetailUiState.Success(
                        tvShow = updatedTvShow,
                        credits = credits,
                        videos = videoResults,
                        similar = similarShows,
                        watchProviders = inProviders,
                        keywords = keywords,
                        reviews = reviews
                    )
                }
            } catch (e: Exception) {
                _uiState.value = TvDetailUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    fun addToWatchlist(status: String = WatchlistStatus.PLAN_TO_WATCH.name) {
        val currentState = _uiState.value
        if (currentState is TvDetailUiState.Success) {
            val tvShow = currentState.tvShow
            val entity = WatchlistEntity(
                id = tvShow.id,
                mediaType = MediaType.TV,
                title = tvShow.name,
                posterPath = tvShow.posterPath,
                voteAverage = tvShow.voteAverage,
                year = tvShow.firstAirDate?.take(4),
                status = status
            )
            viewModelScope.launch {
                watchlistRepository.add(entity)
            }
        }
    }

    fun removeFromWatchlist() {
        viewModelScope.launch {
            watchlistRepository.remove(tvId, MediaType.TV)
        }
    }

    fun updateWatchlistStatus(status: String) {
        viewModelScope.launch {
            watchlistRepository.updateStatus(tvId, MediaType.TV, status)
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

    fun hideItem() {
        val currentState = _uiState.value
        if (currentState is TvDetailUiState.Success) {
            viewModelScope.launch {
                hiddenItemRepository.hide(tvId, "TV", currentState.tvShow.name)
            }
        }
    }
}
