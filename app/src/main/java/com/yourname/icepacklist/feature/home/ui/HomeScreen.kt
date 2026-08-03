package com.yourname.icepacklist.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    onSettingsClick: () -> Unit = {},
    onScrollUp: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf(stringResource(R.string.tab_all), stringResource(R.string.tab_movies), stringResource(R.string.tab_tv_shows))

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()

    val isScrollingUp by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 || !listState.canScrollBackward }
    }
    
    LaunchedEffect(isScrollingUp) {
        onScrollUp(isScrollingUp)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title), color = MaterialTheme.colorScheme.onSurface) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = uiState.selectedTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFE50914) else MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.setSelectedTab(index) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                } else if (uiState.isError) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WifiOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No internet connection",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Check your connection and try again",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        when (uiState.selectedTab) {
                            0 -> {
                                item(key = "all_trending_movies", contentType = "CategoryRow") {
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
                                item(key = "all_recommendations", contentType = "CategoryRow") {
                                    if (uiState.recommendations.isNotEmpty()) {
                                        CategoryRow(
                                            title = "Recommended",
                                            items = uiState.recommendations,
                                            onViewAll = { onViewCategory("recommendations") },
                                            key = { item -> 
                                                if (item is com.yourname.icepacklist.feature.home.domain.Movie) item.id else if (item is com.yourname.icepacklist.feature.home.domain.TvShow) item.id else item.hashCode()
                                            },
                                            itemContent = { item -> 
                                                if (item is com.yourname.icepacklist.feature.home.domain.Movie) {
                                                    MovieCard(item, onClick = { onMovieClick(item.id) })
                                                } else if (item is com.yourname.icepacklist.feature.home.domain.TvShow) {
                                                    TvShowCard(item, onClick = { onTvShowClick(item.id) })
                                                }
                                            }
                                        )
                                    }
                                }
                                item(key = "all_popular_movies", contentType = "CategoryRow") {
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
                                item(key = "all_now_playing_movies", contentType = "CategoryRow") {
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
                                item(key = "all_upcoming_movies", contentType = "CategoryRow") {
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
                                item(key = "all_top_rated_movies", contentType = "CategoryRow") {
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
                                item(key = "all_trending_tv", contentType = "CategoryRow") {
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
                                item(key = "all_popular_tv", contentType = "CategoryRow") {
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
                                item(key = "all_top_rated_tv", contentType = "CategoryRow") {
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
                                item(key = "all_airing_today_tv", contentType = "CategoryRow") {
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
                                item(key = "movies_trending", contentType = "CategoryRow") {
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
                                item(key = "movies_recommendations", contentType = "CategoryRow") {
                                    if (uiState.recommendations.isNotEmpty()) {
                                        CategoryRow(
                                            title = "Recommended",
                                            items = uiState.recommendations,
                                            onViewAll = { onViewCategory("recommendations") },
                                            key = { item -> 
                                                if (item is com.yourname.icepacklist.feature.home.domain.Movie) item.id else if (item is com.yourname.icepacklist.feature.home.domain.TvShow) item.id else item.hashCode()
                                            },
                                            itemContent = { item -> 
                                                if (item is com.yourname.icepacklist.feature.home.domain.Movie) {
                                                    MovieCard(item, onClick = { onMovieClick(item.id) })
                                                } else if (item is com.yourname.icepacklist.feature.home.domain.TvShow) {
                                                    TvShowCard(item, onClick = { onTvShowClick(item.id) })
                                                }
                                            }
                                        )
                                    }
                                }
                                item(key = "movies_popular", contentType = "CategoryRow") {
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
                                item(key = "movies_now_playing", contentType = "CategoryRow") {
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
                                item(key = "movies_upcoming", contentType = "CategoryRow") {
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
                                item(key = "movies_top_rated", contentType = "CategoryRow") {
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
                                item(key = "tv_trending", contentType = "CategoryRow") {
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
                                item(key = "tv_recommendations", contentType = "CategoryRow") {
                                    if (uiState.recommendations.isNotEmpty()) {
                                        CategoryRow(
                                            title = "Recommended",
                                            items = uiState.recommendations,
                                            onViewAll = { onViewCategory("recommendations") },
                                            key = { item -> 
                                                if (item is com.yourname.icepacklist.feature.home.domain.Movie) item.id else if (item is com.yourname.icepacklist.feature.home.domain.TvShow) item.id else item.hashCode()
                                            },
                                            itemContent = { item -> 
                                                if (item is com.yourname.icepacklist.feature.home.domain.Movie) {
                                                    MovieCard(item, onClick = { onMovieClick(item.id) })
                                                } else if (item is com.yourname.icepacklist.feature.home.domain.TvShow) {
                                                    TvShowCard(item, onClick = { onTvShowClick(item.id) })
                                                }
                                            }
                                        )
                                    }
                                }
                                item(key = "tv_popular", contentType = "CategoryRow") {
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
                                item(key = "tv_top_rated", contentType = "CategoryRow") {
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
                                item(key = "tv_airing_today", contentType = "CategoryRow") {
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
