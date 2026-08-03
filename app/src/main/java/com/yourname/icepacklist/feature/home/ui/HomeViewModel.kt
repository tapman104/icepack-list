package com.yourname.icepacklist.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun retry() {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false, errorMessage = "", error = null) }
            
            val trendingMoviesDef = async { repository.getTrendingMovies() }
            val popularMoviesDef = async { repository.getPopularMovies() }
            val nowPlayingMoviesDef = async { repository.getNowPlayingMovies() }
            val upcomingMoviesDef = async { repository.getUpcomingMovies() }
            val topRatedMoviesDef = async { repository.getTopRatedMovies() }
            
            val trendingTvShowsDef = async { repository.getTrendingTvShows() }
            val popularTvShowsDef = async { repository.getPopularTvShows() }
            val topRatedTvShowsDef = async { repository.getTopRatedTvShows() }
            val airingTodayTvShowsDef = async { repository.getAiringTodayTvShows() }
            
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
                        trendingMovies = trendingMoviesDef.await().getOrDefault(emptyList()),
                        popularMovies = popularMoviesDef.await().getOrDefault(emptyList()),
                        nowPlayingMovies = nowPlayingMoviesDef.await().getOrDefault(emptyList()),
                        upcomingMovies = upcomingMoviesDef.await().getOrDefault(emptyList()),
                        topRatedMovies = topRatedMoviesDef.await().getOrDefault(emptyList()),
                        trendingTvShows = trendingTvShowsDef.await().getOrDefault(emptyList()),
                        popularTvShows = popularTvShowsDef.await().getOrDefault(emptyList()),
                        topRatedTvShows = topRatedTvShowsDef.await().getOrDefault(emptyList()),
                        airingTodayTvShows = airingTodayTvShowsDef.await().getOrDefault(emptyList()),
                    )
                }
            }
        }
    }
}
