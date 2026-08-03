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
import com.yourname.icepacklist.feature.home.domain.Review
import com.yourname.icepacklist.feature.home.domain.TvShow
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
import com.yourname.icepacklist.feature.home.domain.VideoResult
import com.yourname.icepacklist.feature.home.domain.WatchProvider

@Composable
fun TvDetailScreen(
    viewModel: TvDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onTvShowClick: (Int) -> Unit = {},
    onPersonClick: (Int) -> Unit = {},
    onSeasonClick: (Int, String, Int) -> Unit = { _, _, _ -> },
    onFullCastClick: (Int, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val entryState by viewModel.entryState.collectAsState()
    val primaryRed = remember { Color(0xFFE50914) }

    // L1 — outer Box(fillMaxSize) removed to eliminate double measurement with LazyColumn(fillMaxSize).
    // Loading/Error each have their own Box with centering; Success renders directly.
    when (val state = uiState) {
        is TvDetailUiState.Loading -> {
            DetailScreenShimmer()
        }
        is TvDetailUiState.Error -> {
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
                        onClick = { viewModel.loadData() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryRed)
                    ) {
                        Text(stringResource(R.string.retry), color = Color.White)
                    }
                }
            }
        }
        is TvDetailUiState.Success -> {
            TvDetailContent(
                tvShow = state.tvShow,
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
                onTvShowClick = onTvShowClick,
                onPersonClick = onPersonClick,
                onSeasonClick = onSeasonClick,
                onFullCastClick = onFullCastClick
            )
        }
    }
}

@Composable
private fun TvDetailContent(
    tvShow: TvShowDetail,
    credits: CreditsResponse,
    videos: List<VideoResult>,
    similar: List<TvShow>,
    watchProviders: List<WatchProvider>,
    keywords: List<Keyword>,
    reviews: List<Review>,
    entryState: WatchlistEntity?,
    onAddToWatchlist: (String) -> Unit,
    onSaveEntry: (WatchlistEntity) -> Unit,
    onRemoveEntry: (WatchlistEntity) -> Unit,
    onBack: () -> Unit,
    onTvShowClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    onSeasonClick: (Int, String, Int) -> Unit,
    onFullCastClick: (Int, String) -> Unit
) {
    var showMyListSheet by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TvDetailHeroSection(
                tvShow = tvShow,
                entryState = entryState,
                onBack = onBack,
                onAddToWatchlist = onAddToWatchlist,
                onShowMyListSheet = { showMyListSheet = true }
            )
        }
        item {
            TvDetailMediaSection(
                overview = tvShow.overview,
                watchProviders = watchProviders,
                videos = videos
            )
        }
        item {
            TvDetailCastSection(
                credits = credits,
                mediaId = tvShow.id,
                mediaType = "tv",
                onPersonClick = onPersonClick,
                onFullCastClick = onFullCastClick
            )
        }
        item {
            TvDetailInfoSection(
                tvShow = tvShow,
                keywords = keywords,
                onSeasonClick = onSeasonClick
            )
        }
        item {
            TvDetailRecommendationsSection(
                similar = similar,
                reviews = reviews,
                onTvShowClick = onTvShowClick
            )
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showMyListSheet && entryState != null) {
        MyListSheet(
            entry = entryState,
            mediaType = MediaType.TV,
            totalEpisodes = tvShow.numberOfEpisodes,
            onSave = onSaveEntry,
            onRemove = { onRemoveEntry(entryState) },
            onDismiss = { showMyListSheet = false }
        )
    }
}
