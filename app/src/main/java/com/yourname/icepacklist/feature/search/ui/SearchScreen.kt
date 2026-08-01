package com.yourname.icepacklist.feature.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.yourname.icepacklist.core.ui.MovieCard
import com.yourname.icepacklist.core.ui.ShimmerGrid
import com.yourname.icepacklist.feature.home.domain.Movie

private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w342"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val movies = viewModel.searchResults.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChange
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                movies.loadState.refresh is LoadState.Loading && searchQuery.isNotBlank() -> ShimmerGrid()
                movies.loadState.refresh is LoadState.Error -> {
                    val error = (movies.loadState.refresh as LoadState.Error).error
                    FullScreenError(
                        message = error.localizedMessage ?: "Unknown error",
                        onRetry = { movies.retry() }
                    )
                }
                searchQuery.isBlank() -> EmptySearchState()
                movies.itemCount == 0 && movies.loadState.refresh is LoadState.NotLoading -> NoResultsState()
                else -> MovieGrid(movies, onMovieClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search movies...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1C1C1E),
            unfocusedContainerColor = Color(0xFF1C1C1E),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFFE50914),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = CircleShape,
        singleLine = true
    )
}

@Composable
private fun EmptySearchState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color(0xFF3A3A3C),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Type something to search",
                color = Color(0xFF888888),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun NoResultsState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color(0xFF3A3A3C),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No movies found",
                color = Color(0xFF888888),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun MovieGrid(movies: LazyPagingItems<Movie>, onMovieClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = movies.itemCount,
            key = { index -> movies.peek(index)?.id ?: index }
        ) { index ->
            val movie = movies[index]
            if (movie != null) {
                MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
            } else {
                PosterPlaceholder()
            }
        }

        // Append loading indicator
        if (movies.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE50914),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1E))
    )
}

@Composable
private fun FullScreenError(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚠\uFE0F Failed to load",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = message,
                color = Color(0xFF888888),
                style = MaterialTheme.typography.bodySmall
            )
            if (message.contains("401") || message.contains("403")) {
                Text(
                    text = "Check your API key in Settings.",
                    color = Color(0xFFE50914),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}
