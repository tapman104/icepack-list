package com.yourname.icepacklist.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.icepacklist.core.ui.CategoryRow
import com.yourname.icepacklist.core.ui.CategoryShimmerRow
import com.yourname.icepacklist.core.ui.MovieCard
import com.yourname.icepacklist.core.ui.TvShowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit = {},
    onViewCategory: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Icepack List", color = Color.White) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D)
                )
            )
        },
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D))
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(3) {
                        CategoryShimmerRow()
                    }
                }
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = uiState.error ?: "Unknown error", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Retry")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        if (uiState.trendingMovies.isNotEmpty()) {
                            CategoryRow(
                                title = "Trending This Week",
                                items = uiState.trendingMovies,
                                onViewAll = { onViewCategory("trending_movies") },
                                itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                            )
                        }
                    }
                    item {
                        if (uiState.popularMovies.isNotEmpty()) {
                            CategoryRow(
                                title = "Popular Movies",
                                items = uiState.popularMovies,
                                onViewAll = { onViewCategory("popular_movies") },
                                itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                            )
                        }
                    }
                    item {
                        if (uiState.nowPlayingMovies.isNotEmpty()) {
                            CategoryRow(
                                title = "Now Playing",
                                items = uiState.nowPlayingMovies,
                                onViewAll = { onViewCategory("now_playing") },
                                itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                            )
                        }
                    }
                    item {
                        if (uiState.upcomingMovies.isNotEmpty()) {
                            CategoryRow(
                                title = "Upcoming",
                                items = uiState.upcomingMovies,
                                onViewAll = { onViewCategory("upcoming") },
                                itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                            )
                        }
                    }
                    item {
                        if (uiState.topRatedMovies.isNotEmpty()) {
                            CategoryRow(
                                title = "Top Rated Movies",
                                items = uiState.topRatedMovies,
                                onViewAll = { onViewCategory("top_rated_movies") },
                                itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                            )
                        }
                    }
                    item {
                        if (uiState.trendingTvShows.isNotEmpty()) {
                            CategoryRow(
                                title = "Trending Shows",
                                items = uiState.trendingTvShows,
                                onViewAll = { onViewCategory("trending_tv") },
                                itemContent = { tvShow -> TvShowCard(tvShow, onClick = { /* TODO TvShowDetail */ }) }
                            )
                        }
                    }
                    item {
                        if (uiState.popularTvShows.isNotEmpty()) {
                            CategoryRow(
                                title = "Popular Shows",
                                items = uiState.popularTvShows,
                                onViewAll = { onViewCategory("popular_tv") },
                                itemContent = { tvShow -> TvShowCard(tvShow, onClick = { /* TODO */ }) }
                            )
                        }
                    }
                    item {
                        if (uiState.topRatedTvShows.isNotEmpty()) {
                            CategoryRow(
                                title = "Top Rated Shows",
                                items = uiState.topRatedTvShows,
                                onViewAll = { onViewCategory("top_rated_tv") },
                                itemContent = { tvShow -> TvShowCard(tvShow, onClick = { /* TODO */ }) }
                            )
                        }
                    }
                    item {
                        if (uiState.airingTodayTvShows.isNotEmpty()) {
                            CategoryRow(
                                title = "Airing Today",
                                items = uiState.airingTodayTvShows,
                                onViewAll = { onViewCategory("airing_today") },
                                itemContent = { tvShow -> TvShowCard(tvShow, onClick = { /* TODO */ }) }
                            )
                        }
                    }
                }
            }
        }
    }
}
