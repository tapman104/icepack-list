package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.icepacklist.R
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.home.domain.Keyword
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.feature.home.domain.Review
import com.yourname.icepacklist.feature.home.domain.VideoResult
import com.yourname.icepacklist.feature.home.domain.WatchProvider

@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onMovieClick: (Int) -> Unit = {},
    onPersonClick: (Int) -> Unit = {},
    onFullCastClick: (Int, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val entryState by viewModel.entryState.collectAsState()

    // L1 — outer Box(fillMaxSize) removed to eliminate double measurement with LazyColumn(fillMaxSize).
    // Loading/Error each have their own Box with centering; Success renders directly.
    when (val state = uiState) {
        is DetailUiState.Loading -> {
            DetailScreenShimmer()
        }
        is DetailUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.error_loading_details),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadMovieDetail() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
                    ) {
                        Text(stringResource(R.string.retry), color = Color.White)
                    }
                }
            }
        }
        is DetailUiState.Success -> {
            DetailContent(
                movie = state.movie,
                credits = state.credits,
                videos = state.videos,
                similar = state.similar,
                watchProviders = state.watchProviders,
                keywords = state.keywords,
                reviews = state.reviews,
                entryState = entryState,
                onAddToWatchlist = { viewModel.addToWatchlist(it) },
                onSaveEntry = { viewModel.saveEntry(it) },
                onRemoveEntry = { viewModel.removeEntry(it) },
                onBack = onBack,
                onMovieClick = onMovieClick,
                onPersonClick = onPersonClick,
                onFullCastClick = onFullCastClick
            )
        }
    }
}

@Composable
private fun DetailContent(
    movie: MovieDetail,
    credits: CreditsResponse,
    videos: List<VideoResult>,
    similar: List<Movie>,
    watchProviders: List<WatchProvider>,
    keywords: List<Keyword>,
    reviews: List<Review>,
    entryState: WatchlistEntity?,
    onAddToWatchlist: (String) -> Unit,
    onSaveEntry: (WatchlistEntity) -> Unit,
    onRemoveEntry: (WatchlistEntity) -> Unit,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    onFullCastClick: (Int, String) -> Unit
) {
    var showMyListSheet by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            DetailHeroSection(
                movie = movie,
                entryState = entryState,
                onBack = onBack,
                onAddToWatchlist = onAddToWatchlist,
                onShowMyListSheet = { showMyListSheet = true }
            )
        }
        item {
            DetailMediaSection(
                overview = movie.overview,
                watchProviders = watchProviders,
                videos = videos
            )
        }
        item {
            DetailCastSection(
                credits = credits,
                director = movie.director,
                mediaId = movie.id,
                mediaType = "movie",
                onPersonClick = onPersonClick,
                onFullCastClick = onFullCastClick
            )
        }
        item {
            DetailInfoSection(
                movie = movie,
                keywords = keywords
            )
        }
        item {
            DetailRecommendationsSection(
                similar = similar,
                reviews = reviews,
                onMovieClick = onMovieClick
            )
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showMyListSheet && entryState != null) {
        MyListSheet(
            entry = entryState,
            mediaType = MediaType.MOVIE,
            onSave = onSaveEntry,
            onRemove = { onRemoveEntry(entryState) },
            onDismiss = { showMyListSheet = false }
        )
    }
}
