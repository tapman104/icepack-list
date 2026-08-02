package com.yourname.icepacklist.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.icepacklist.R
import com.yourname.icepacklist.core.ui.CategoryRow
import com.yourname.icepacklist.core.ui.CategoryShimmerRow
import com.yourname.icepacklist.core.ui.MovieCard
import com.yourname.icepacklist.core.ui.TvShowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit = {},
    onTvShowClick: (Int) -> Unit = {},
    onViewCategory: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.tab_all), stringResource(R.string.tab_movies), stringResource(R.string.tab_tv_shows))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title), color = Color.White) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D)
                )
            )
        },
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D))
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFE50914) else Color(0xFF1C1C1E))
                            .clickable { selectedTab = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else Color(0xFF888888),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
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
                        Text(text = uiState.error ?: stringResource(R.string.unknown_error), color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                item {
                                    if (uiState.trendingMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_trending_week),
                                            items = uiState.trendingMovies,
                                            onViewAll = { onViewCategory("trending_movies") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.popularMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_popular_movies),
                                            items = uiState.popularMovies,
                                            onViewAll = { onViewCategory("popular_movies") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.nowPlayingMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_now_playing),
                                            items = uiState.nowPlayingMovies,
                                            onViewAll = { onViewCategory("now_playing") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.upcomingMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_upcoming),
                                            items = uiState.upcomingMovies,
                                            onViewAll = { onViewCategory("upcoming") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.topRatedMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_top_rated_movies),
                                            items = uiState.topRatedMovies,
                                            onViewAll = { onViewCategory("top_rated_movies") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.trendingTvShows.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_trending_shows),
                                            items = uiState.trendingTvShows,
                                            onViewAll = { onViewCategory("trending_tv") },
                                            key = { it.id },
                                            itemContent = { tvShow -> TvShowCard(tvShow, onClick = { onTvShowClick(tvShow.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.popularTvShows.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_popular_shows),
                                            items = uiState.popularTvShows,
                                            onViewAll = { onViewCategory("popular_tv") },
                                            key = { it.id },
                                            itemContent = { tvShow -> TvShowCard(tvShow, onClick = { onTvShowClick(tvShow.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.topRatedTvShows.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_top_rated_shows),
                                            items = uiState.topRatedTvShows,
                                            onViewAll = { onViewCategory("top_rated_tv") },
                                            key = { it.id },
                                            itemContent = { tvShow -> TvShowCard(tvShow, onClick = { onTvShowClick(tvShow.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.airingTodayTvShows.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_airing_today),
                                            items = uiState.airingTodayTvShows,
                                            onViewAll = { onViewCategory("airing_today") },
                                            key = { it.id },
                                            itemContent = { tvShow -> TvShowCard(tvShow, onClick = { onTvShowClick(tvShow.id) }) }
                                        )
                                    }
                                }
                            }
                            1 -> {
                                item {
                                    if (uiState.trendingMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_trending_week),
                                            items = uiState.trendingMovies,
                                            onViewAll = { onViewCategory("trending_movies") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.popularMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_popular_movies),
                                            items = uiState.popularMovies,
                                            onViewAll = { onViewCategory("popular_movies") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.nowPlayingMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_now_playing),
                                            items = uiState.nowPlayingMovies,
                                            onViewAll = { onViewCategory("now_playing") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.upcomingMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_upcoming),
                                            items = uiState.upcomingMovies,
                                            onViewAll = { onViewCategory("upcoming") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.topRatedMovies.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_top_rated_movies),
                                            items = uiState.topRatedMovies,
                                            onViewAll = { onViewCategory("top_rated_movies") },
                                            key = { it.id },
                                            itemContent = { movie -> MovieCard(movie, onClick = { onMovieClick(movie.id) }) }
                                        )
                                    }
                                }
                            }
                            2 -> {
                                item {
                                    if (uiState.trendingTvShows.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_trending_shows),
                                            items = uiState.trendingTvShows,
                                            onViewAll = { onViewCategory("trending_tv") },
                                            key = { it.id },
                                            itemContent = { tvShow -> TvShowCard(tvShow, onClick = { onTvShowClick(tvShow.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.popularTvShows.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_popular_shows),
                                            items = uiState.popularTvShows,
                                            onViewAll = { onViewCategory("popular_tv") },
                                            key = { it.id },
                                            itemContent = { tvShow -> TvShowCard(tvShow, onClick = { onTvShowClick(tvShow.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.topRatedTvShows.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_top_rated_shows),
                                            items = uiState.topRatedTvShows,
                                            onViewAll = { onViewCategory("top_rated_tv") },
                                            key = { it.id },
                                            itemContent = { tvShow -> TvShowCard(tvShow, onClick = { onTvShowClick(tvShow.id) }) }
                                        )
                                    }
                                }
                                item {
                                    if (uiState.airingTodayTvShows.isNotEmpty()) {
                                        CategoryRow(
                                            title = stringResource(R.string.section_airing_today),
                                            items = uiState.airingTodayTvShows,
                                            onViewAll = { onViewCategory("airing_today") },
                                            key = { it.id },
                                            itemContent = { tvShow -> TvShowCard(tvShow, onClick = { onTvShowClick(tvShow.id) }) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
