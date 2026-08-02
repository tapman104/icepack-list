package com.yourname.icepacklist.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.yourname.icepacklist.core.ui.MovieCard
import com.yourname.icepacklist.core.ui.ShimmerGrid
import com.yourname.icepacklist.core.ui.TvShowCard
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.TvShow
import androidx.compose.ui.res.stringResource
import com.yourname.icepacklist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    viewModel: CategoryListViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onTvShowClick: (Int) -> Unit
) {
    val items = viewModel.items.collectAsLazyPagingItems()
    val title = when (viewModel.category) {
        "trending_movies" -> stringResource(R.string.section_trending_week)
        "popular_movies" -> stringResource(R.string.section_popular_movies)
        "now_playing" -> stringResource(R.string.section_now_playing)
        "upcoming" -> stringResource(R.string.section_upcoming)
        "top_rated_movies" -> stringResource(R.string.section_top_rated_movies)
        "trending_tv" -> stringResource(R.string.section_trending_shows)
        "popular_tv" -> stringResource(R.string.section_popular_shows)
        "top_rated_tv" -> stringResource(R.string.section_top_rated_shows)
        "airing_today" -> stringResource(R.string.section_airing_today)
        else -> stringResource(R.string.category_default)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D0D))
            )
        },
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D)).padding(padding)) {
            when {
                items.loadState.refresh is LoadState.Loading -> ShimmerGrid()
                items.loadState.refresh is LoadState.Error -> {
                    val error = (items.loadState.refresh as LoadState.Error).error
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = error.localizedMessage ?: "Unknown error", color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { items.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                items.itemCount == 0 && items.loadState.refresh is LoadState.NotLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items found", color = Color.Gray)
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = items.itemCount,
                            key = { index -> 
                                val item = items.peek(index)
                                if (item is Movie) "movie_${item.id}" else if (item is TvShow) "tv_${item.id}" else index
                            },
                            contentType = { index ->
                                val item = items.peek(index)
                                if (item is Movie) "movie" else if (item is TvShow) "tv" else null
                            }
                        ) { index ->
                            val item = items[index]
                            if (item is Movie) {
                                MovieCard(movie = item, onClick = { onMovieClick(item.id) })
                            } else if (item is TvShow) {
                                TvShowCard(tvShow = item, onClick = { onTvShowClick(item.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

