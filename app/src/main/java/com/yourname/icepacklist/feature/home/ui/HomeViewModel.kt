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
    val trendingTvShows: List<TvShow> = emptyList(),
    val popularTvShows: List<TvShow> = emptyList(),
    val topRatedTvShows: List<TvShow> = emptyList(),
    val airingTodayTvShows: List<TvShow> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val apiKeyDataStore: ApiKeyDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var lastFilter: ContentFilter = ContentFilter.ALL

    init {
        viewModelScope.launch {
            apiKeyDataStore.contentFilter.collect { key ->
                val filter = ContentFilter.fromKey(key)
                lastFilter = filter
                refresh(filter)
            }
        }
    }

    fun retry() {
        refresh(lastFilter)
    }

    fun refresh(filter: ContentFilter = lastFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false, errorMessage = "", error = null) }
            
            val trendingMoviesDef = async { repository.getTrendingMovies(filter) }
            val popularMoviesDef = async { repository.getPopularMovies(filter) }
            val nowPlayingMoviesDef = async { repository.getNowPlayingMovies(filter) }
            val upcomingMoviesDef = async { repository.getUpcomingMovies(filter) }
            val topRatedMoviesDef = async { repository.getTopRatedMovies(filter) }
            
            val trendingTvShowsDef = async { repository.getTrendingTvShows(filter) }
            val popularTvShowsDef = async { repository.getPopularTvShows(filter) }
            val topRatedTvShowsDef = async { repository.getTopRatedTvShows(filter) }
            val airingTodayTvShowsDef = async { repository.getAiringTodayTvShows(filter) }
            
            val results = awaitAll(
                trendingMoviesDef, popularMoviesDef, nowPlayingMoviesDef, upcomingMoviesDef, topRatedMoviesDef,
                trendingTvShowsDef, popularTvShowsDef, topRatedTvShowsDef, airingTodayTvShowsDef
            )
            
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
                        trendingMovies = trendingMoviesDef.await().getOrDefault(emptyList()).distinctBy { it.id },
                        popularMovies = popularMoviesDef.await().getOrDefault(emptyList()).distinctBy { it.id },
                        nowPlayingMovies = nowPlayingMoviesDef.await().getOrDefault(emptyList()).distinctBy { it.id },
                        upcomingMovies = upcomingMoviesDef.await().getOrDefault(emptyList()).distinctBy { it.id },
                        topRatedMovies = topRatedMoviesDef.await().getOrDefault(emptyList()).distinctBy { it.id },
                        trendingTvShows = trendingTvShowsDef.await().getOrDefault(emptyList()).distinctBy { it.id },
                        popularTvShows = popularTvShowsDef.await().getOrDefault(emptyList()).distinctBy { it.id },
                        topRatedTvShows = topRatedTvShowsDef.await().getOrDefault(emptyList()).distinctBy { it.id },
                        airingTodayTvShows = airingTodayTvShowsDef.await().getOrDefault(emptyList()).distinctBy { it.id },
                    )
                }
            }
        }
    }
}

