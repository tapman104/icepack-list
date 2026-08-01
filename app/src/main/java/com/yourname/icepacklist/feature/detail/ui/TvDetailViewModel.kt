package com.yourname.icepacklist.feature.detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.core.database.WatchStatus
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.home.domain.TvShow
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
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

sealed interface TvDetailUiState {
    data object Loading : TvDetailUiState
    data class Error(val message: String) : TvDetailUiState
    data class Success(
        val tvShow: TvShowDetail,
        val credits: CreditsResponse,
        val videos: List<VideoResult>,
        val similar: List<TvShow>
    ) : TvDetailUiState
}

@HiltViewModel
class TvDetailViewModel @Inject constructor(
    private val apiService: TmdbApiService,
    private val watchlistRepository: WatchlistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tvId: Int = checkNotNull(savedStateHandle[Routes.TvDetail.ARG_TV_ID])

    private val _uiState = MutableStateFlow<TvDetailUiState>(TvDetailUiState.Loading)
    val uiState: StateFlow<TvDetailUiState> = _uiState.asStateFlow()

    val isInWatchlist: StateFlow<Boolean> = watchlistRepository.getAll()
        .map { list -> list.any { it.id == tvId && it.mediaType == MediaType.TV } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val watchlistStatus: StateFlow<WatchStatus?> = watchlistRepository.getAll()
        .map { list -> list.firstOrNull { it.id == tvId && it.mediaType == MediaType.TV }?.status }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = TvDetailUiState.Loading
            try {
                val tvDef = async { apiService.getTvShowDetails(tvId) }
                val creditsDef = async { apiService.getTvShowCredits(tvId) }
                val videosDef = async { apiService.getTvShowVideos(tvId) }
                val similarDef = async { apiService.getSimilarTvShows(tvId) }

                awaitAll(tvDef, creditsDef, videosDef, similarDef)

                val tvShow = tvDef.await()
                val credits = creditsDef.await()
                val videosResponse = videosDef.await()
                val similarResponse = similarDef.await()

                val videoResults = videosResponse.results
                    .filter { it.type == "Trailer" && it.site == "YouTube" }
                    .map { VideoResult(key = it.key, name = it.name, site = it.site, type = it.type) }
                val similarShows = similarResponse.results.take(12)

                val networkNames = tvShow.networksList.map { it.name }
                val createdByStr = tvShow.createdByList.map { it.name }.joinToString(", ")

                val updatedTvShow = tvShow.copy(
                    networks = networkNames,
                    createdBy = createdByStr,
                    videos = videoResults,
                    similar = similarShows
                )

                _uiState.value = TvDetailUiState.Success(
                    tvShow = updatedTvShow,
                    credits = credits,
                    videos = videoResults,
                    similar = similarShows
                )
            } catch (e: Exception) {
                _uiState.value = TvDetailUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    fun addToWatchlist(status: WatchStatus = WatchStatus.PLANNING) {
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

    fun updateWatchlistStatus(status: WatchStatus) {
        viewModelScope.launch {
            watchlistRepository.updateStatus(tvId, MediaType.TV, status)
        }
    }
}
