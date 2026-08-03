package com.yourname.icepacklist.feature.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yourname.icepacklist.core.ui.ShimmerGrid
import com.yourname.icepacklist.feature.home.domain.MultiSearchResult

private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w342"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: SearchViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit = {},
    onTvShowClick: (Int) -> Unit = {},
    onPersonClick: (Int) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val results = viewModel.searchResults.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .statusBarsPadding()
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChange
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                results.loadState.refresh is LoadState.Loading && searchQuery.isNotBlank() -> SearchResultShimmer()
                results.loadState.refresh is LoadState.Error -> {
                    val error = (results.loadState.refresh as LoadState.Error).error
                    FullScreenError(
                        message = error.localizedMessage ?: "Unknown error",
                        onRetry = { results.retry() }
                    )
                }
                searchQuery.isBlank() -> {
                    if (searchHistory.isEmpty()) {
                        EmptySearchState()
                    } else {
                        SearchHistoryList(
                            history = searchHistory,
                            onQueryClick = viewModel::onSearchQueryChange,
                            onRemoveClick = viewModel::removeSearchHistoryItem,
                            onClearAllClick = viewModel::clearSearchHistory
                        )
                    }
                }
                results.itemCount == 0 && results.loadState.refresh is LoadState.NotLoading -> NoResultsState()
                else -> SearchResultGrid(results, onMovieClick, onTvShowClick, onPersonClick)
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
        placeholder = { Text("Search...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFFE50914),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun SearchHistoryList(
    history: List<com.yourname.icepacklist.core.database.entity.SearchHistoryEntity>,
    onQueryClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit,
    onClearAllClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onClearAllClick) {
                Text("Clear All")
            }
        }
        
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(history.size, key = { history[it].query }) { index ->
                val item = history[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQueryClick(item.query) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.query,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemoveClick(item.query) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
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
                text = "No results found",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun SearchResultGrid(
    results: LazyPagingItems<MultiSearchResult>,
    onMovieClick: (Int) -> Unit,
    onTvShowClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = results.itemCount,
            key = { index -> 
                val item = results.peek(index)
                if (item != null) "${item.mediaType}_${item.id}_$index" else index 
            },
            contentType = { index -> results.peek(index)?.mediaType }
        ) { index ->
            val item = results[index]
            if (item != null) {
                SearchResultCard(
                    item = item,
                    onClick = {
                        when (item.mediaType) {
                            "movie" -> onMovieClick(item.id)
                            "tv" -> onTvShowClick(item.id)
                            "person" -> onPersonClick(item.id)
                        }
                    }
                )
            } else {
                PosterPlaceholder()
            }
        }

        if (results.loadState.append is LoadState.Loading) {
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
private fun SearchResultCard(
    item: MultiSearchResult,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (item.displayPoster.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(TMDB_IMAGE_BASE + item.displayPoster)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            val chipColor = when (item.mediaType) {
                "movie" -> Color(0xFF673AB7)
                "tv" -> Color(0xFF009688)
                "person" -> Color(0xFF757575)
                else -> Color(0xFF333333)
            }
            val chipColorFaded = remember(item.mediaType) { chipColor.copy(alpha = 0.9f) }
            
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(chipColorFaded, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = item.mediaType.uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.displayTitle,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        if (item.displayDate.isNotBlank()) {
            Text(
                text = item.displayDate.take(4),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
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
            .background(MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun FullScreenError(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            if (message.contains("401") || message.contains("403")) {
                Text(
                    text = "Check your API key in Settings.",
                    color = Color(0xFFE50914),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun SearchResultShimmer() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(10) {
            Column {
                com.yourname.icepacklist.core.ui.ShimmerBox(
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f),
                    cornerRadius = 8.dp
                )
                Spacer(modifier = Modifier.height(6.dp))
                com.yourname.icepacklist.core.ui.ShimmerBox(modifier = Modifier.width(50.dp).height(18.dp), cornerRadius = 9.dp)
                Spacer(modifier = Modifier.height(4.dp))
                com.yourname.icepacklist.core.ui.ShimmerBox(modifier = Modifier.fillMaxWidth(0.8f).height(12.dp), cornerRadius = 4.dp)
                Spacer(modifier = Modifier.height(4.dp))
                com.yourname.icepacklist.core.ui.ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(10.dp), cornerRadius = 4.dp)
            }
        }
    }
}
