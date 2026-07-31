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
        "trending_movies" -> "Trending This Week"
        "popular_movies" -> "Popular Movies"
        "now_playing" -> "Now Playing"
        "upcoming" -> "Upcoming"
        "top_rated_movies" -> "Top Rated Movies"
        "trending_tv" -> "Trending Shows"
        "popular_tv" -> "Popular Shows"
        "top_rated_tv" -> "Top Rated Shows"
        "airing_today" -> "Airing Today"
        else -> "Category"
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
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items.itemCount) { index ->
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

