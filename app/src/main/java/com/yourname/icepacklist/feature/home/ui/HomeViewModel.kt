package com.yourname.icepacklist.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.datastore.ApiKeyDataStore
import com.yourname.icepacklist.core.datastore.ContentFilter
import com.yourname.icepacklist.feature.home.data.HomeRepository
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.TvShow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val trendingMovies: List<Movie> = emptyList(),
    val popularMovies: List<Movie> = emptyList(),
    val nowPlayingMovies: List<Movie> = emptyList(),
    val upcomingMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val recommendations: List<Any> = emptyList(),
    val trendingTvShows: List<TvShow> = emptyList(),
    val popularTvShows: List<TvShow> = emptyList(),
    val topRatedTvShows: List<TvShow> = emptyList(),
    val airingTodayTvShows: List<TvShow> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val error: String? = null,
    val selectedTab: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val apiKeyDataStore: ApiKeyDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var lastHomeFilter: Set<ContentFilter> = setOf(ContentFilter.ALL)
    private var lastRecsFilter: Set<ContentFilter> = setOf(ContentFilter.ALL)

    init {
        viewModelScope.launch {
            apiKeyDataStore.homeContentFilter.collect { filter ->
                lastHomeFilter = filter
                refreshHomeRows(filter)
            }
        }
        viewModelScope.launch {
            apiKeyDataStore.recommendationsContentFilter.collect { filter ->
                lastRecsFilter = filter
                refreshRecommendations(filter)
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        if (_uiState.value.selectedTab == tab) return
        _uiState.update { it.copy(selectedTab = tab) }
        refreshHomeRows(lastHomeFilter)
        refreshRecommendations(lastRecsFilter)
    }

    fun retry() {
        refreshHomeRows(lastHomeFilter)
        refreshRecommendations(lastRecsFilter)
    }

    fun refresh() {
        retry()
    }

    private fun refreshHomeRows(filter: Set<ContentFilter>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false, errorMessage = "", error = null) }
            
            val tab = _uiState.value.selectedTab
            val fetchMovies = tab == 0 || tab == 1
            val fetchTv = tab == 0 || tab == 2

            val trendingMoviesDef = if (fetchMovies) async { repository.getTrendingMovies(filter) } else null
            val popularMoviesDef = if (fetchMovies) async { repository.getPopularMovies(filter) } else null
            val nowPlayingMoviesDef = if (fetchMovies) async { repository.getNowPlayingMovies(filter) } else null
            val upcomingMoviesDef = if (fetchMovies) async { repository.getUpcomingMovies(filter) } else null
            val topRatedMoviesDef = if (fetchMovies) async { repository.getTopRatedMovies(filter) } else null
            
            val trendingTvShowsDef = if (fetchTv) async { repository.getTrendingTvShows(filter) } else null
            val popularTvShowsDef = if (fetchTv) async { repository.getPopularTvShows(filter) } else null
            val topRatedTvShowsDef = if (fetchTv) async { repository.getTopRatedTvShows(filter) } else null
            val airingTodayTvShowsDef = if (fetchTv) async { repository.getAiringTodayTvShows(filter) } else null
            
            val results = listOfNotNull(
                trendingMoviesDef, popularMoviesDef, nowPlayingMoviesDef, upcomingMoviesDef, topRatedMoviesDef,
                trendingTvShowsDef, popularTvShowsDef, topRatedTvShowsDef, airingTodayTvShowsDef
            ).awaitAll()
            
            val anyFailure = results.firstOrNull { it.isFailure }
            if (anyFailure != null) {
                _uiState.update { it.copy(
                    isLoading = false, 
                    isError = true, 
                    errorMessage = "No internet connection", 
                    error = anyFailure.exceptionOrNull()?.message ?: "Unknown error"
                ) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trendingMovies = trendingMoviesDef?.await()?.getOrDefault(emptyList())?.distinctBy { m -> m.id } ?: it.trendingMovies,
                        popularMovies = popularMoviesDef?.await()?.getOrDefault(emptyList())?.distinctBy { m -> m.id } ?: it.popularMovies,
                        nowPlayingMovies = nowPlayingMoviesDef?.await()?.getOrDefault(emptyList())?.distinctBy { m -> m.id } ?: it.nowPlayingMovies,
                        upcomingMovies = upcomingMoviesDef?.await()?.getOrDefault(emptyList())?.distinctBy { m -> m.id } ?: it.upcomingMovies,
                        topRatedMovies = topRatedMoviesDef?.await()?.getOrDefault(emptyList())?.distinctBy { m -> m.id } ?: it.topRatedMovies,
                        trendingTvShows = trendingTvShowsDef?.await()?.getOrDefault(emptyList())?.distinctBy { t -> t.id } ?: it.trendingTvShows,
                        popularTvShows = popularTvShowsDef?.await()?.getOrDefault(emptyList())?.distinctBy { t -> t.id } ?: it.popularTvShows,
                        topRatedTvShows = topRatedTvShowsDef?.await()?.getOrDefault(emptyList())?.distinctBy { t -> t.id } ?: it.topRatedTvShows,
                        airingTodayTvShows = airingTodayTvShowsDef?.await()?.getOrDefault(emptyList())?.distinctBy { t -> t.id } ?: it.airingTodayTvShows,
                    )
                }
            }
        }
    }
    
    private fun refreshRecommendations(filter: Set<ContentFilter>) {
        viewModelScope.launch {
            val tab = _uiState.value.selectedTab
            val fetchMovies = tab == 0 || tab == 1
            val fetchTv = tab == 0 || tab == 2

            if (filter.isEmpty() || filter.contains(ContentFilter.ALL)) {
                _uiState.update { it.copy(recommendations = emptyList()) }
                return@launch
            }

            val recommendationsDef = if (fetchMovies || fetchTv) async { repository.getRecommendations(filter) } else null
            
            val result = recommendationsDef?.await()
            if (result != null && result.isSuccess) {
                _uiState.update {
                    it.copy(recommendations = result.getOrDefault(emptyList()))
                }
            } else if (result != null && result.isFailure) {
                _uiState.update {
                    it.copy(recommendations = emptyList())
                }
            }
        }
    }
}
