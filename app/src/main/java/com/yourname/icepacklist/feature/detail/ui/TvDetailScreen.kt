package com.yourname.icepacklist.feature.detail.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yourname.icepacklist.core.ui.TvShowCard
import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.home.domain.TvShow
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
import com.yourname.icepacklist.feature.home.domain.VideoResult
import com.yourname.icepacklist.core.database.WatchStatus
import com.yourname.icepacklist.core.util.formatDate
import com.yourname.icepacklist.R
import java.util.Locale

private const val TMDB_BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"
private const val TMDB_PROFILE_BASE = "https://image.tmdb.org/t/p/w185"

@Composable
fun TvDetailScreen(
    viewModel: TvDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onTvShowClick: (Int) -> Unit = {},
    onPersonClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()
    val watchlistStatus by viewModel.watchlistStatus.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        when (val state = uiState) {
            is TvDetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFE50914)
                )
            }
            is TvDetailUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.error_loading_details),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = Color(0xFF888888),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadData() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
                    ) {
                        Text(stringResource(R.string.retry), color = Color.White)
                    }
                }
            }
            is TvDetailUiState.Success -> {
                TvDetailContent(
                    tvShow = state.tvShow,
                    credits = state.credits,
                    videos = state.videos,
                    similar = state.similar,
                    isInWatchlist = isInWatchlist,
                    watchlistStatus = watchlistStatus,
                    onAddToWatchlist = { viewModel.addToWatchlist(WatchStatus.PLANNING) },
                    onUpdateWatchlistStatus = { viewModel.updateWatchlistStatus(it) },
                    onRemoveFromWatchlist = { viewModel.removeFromWatchlist() },
                    onBack = onBack,
                    onTvShowClick = onTvShowClick,
                    onPersonClick = onPersonClick
                )
            }
        }
    }
}

@Composable
private fun TvDetailContent(
    tvShow: TvShowDetail,
    credits: CreditsResponse,
    videos: List<VideoResult>,
    similar: List<TvShow>,
    isInWatchlist: Boolean,
    watchlistStatus: WatchStatus?,
    onAddToWatchlist: () -> Unit,
    onUpdateWatchlistStatus: (WatchStatus) -> Unit,
    onRemoveFromWatchlist: () -> Unit,
    onBack: () -> Unit,
    onTvShowClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var watchlistMenuExpanded by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = tvShow.backdropPath?.let { "$TMDB_BACKDROP_BASE$it" },
                    contentDescription = "Backdrop for ${tvShow.name}",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_image_placeholder),
                    error = painterResource(R.drawable.ic_image_placeholder),
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0D0D0D))
                            )
                        )
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = 32.dp, start = 16.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    tvShow.posterPath?.let {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w342$it",
                            contentDescription = "Poster",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(80.dp)
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Column {
                        Text(
                            text = tvShow.name,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 34.sp
                        )
                        if (!tvShow.tagline.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tvShow.tagline,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val ratingStr = String.format(Locale.US, "%.1f", tvShow.voteAverage)
                    Text(
                        text = "★ $ratingStr",
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    tvShow.firstAirDate?.takeIf { it.length >= 4 }?.let {
                        Text(text = it.take(4), color = Color(0xFF888888), fontSize = 14.sp)
                    }
                    tvShow.numberOfSeasons?.let {
                        Text(text = "$it Seasons", color = Color(0xFF888888), fontSize = 14.sp)
                    }
                }

                if (tvShow.genres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tvShow.genres.take(3).forEach { genre ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1C1C1E),
                            ) {
                                Text(
                                    text = genre.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Watchlist Button Row between genre chips and overview
                Spacer(modifier = Modifier.height(16.dp))
                if (!isInWatchlist) {
                    Button(
                        onClick = onAddToWatchlist,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.add_to_list), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { watchlistMenuExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.in_my_list), color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = watchlistMenuExpanded,
                            onDismissRequest = { watchlistMenuExpanded = false },
                            modifier = Modifier.background(Color(0xFF2C2C2E))
                        ) {
                            listOf(
                                stringResource(R.string.status_watching) to WatchStatus.WATCHING,
                                stringResource(R.string.status_completed) to WatchStatus.COMPLETED,
                                stringResource(R.string.status_paused) to WatchStatus.PAUSED,
                                stringResource(R.string.status_dropped) to WatchStatus.DROPPED
                            ).forEach { (label, status) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            color = if (watchlistStatus == status) Color(0xFFE50914) else Color.White,
                                            fontWeight = if (watchlistStatus == status) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        watchlistMenuExpanded = false
                                        onUpdateWatchlistStatus(status)
                                    }
                                )
                            }
                            HorizontalDivider(color = Color(0xFF444444))
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remove_from_list), color = Color(0xFFFF453A)) },
                                onClick = {
                                    watchlistMenuExpanded = false
                                    onRemoveFromWatchlist()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = tvShow.overview,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                // Cast Section — circular cards LazyRow, max 15
                if (credits.cast.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.cast),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(credits.cast.take(15)) { person ->
                            Column(
                                modifier = Modifier
                                    .width(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .androidx.compose.foundation.clickable { onPersonClick(person.id) }
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = person.profilePath?.let { "$TMDB_PROFILE_BASE$it" },
                                    contentDescription = person.name,
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.ic_image_placeholder),
                                    error = painterResource(R.drawable.ic_image_placeholder),
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = person.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = person.character ?: "",
                                    color = Color(0xFF888888),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Details Section
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.details),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "First Air Date", value = formatDate(tvShow.firstAirDate))
                if (!tvShow.lastAirDate.isNullOrBlank()) {
                    DetailRow(label = "Last Air Date", value = formatDate(tvShow.lastAirDate))
                }
                if (!tvShow.status.isNullOrBlank()) {
                    DetailRow(label = stringResource(R.string.status), value = tvShow.status)
                }
                if (tvShow.originCountry.isNotEmpty()) {
                    DetailRow(label = stringResource(R.string.country), value = tvShow.originCountry.joinToString(", "))
                }
                if (!tvShow.originalLanguage.isNullOrBlank()) {
                    DetailRow(label = stringResource(R.string.language), value = tvShow.originalLanguage.uppercase())
                }
                tvShow.numberOfSeasons?.let {
                    DetailRow(label = "Seasons", value = "$it")
                }
                tvShow.numberOfEpisodes?.let {
                    DetailRow(label = "Episodes", value = "$it")
                }
                val networkNames = tvShow.networks.ifEmpty { tvShow.networksList.map { it.name } }
                if (networkNames.isNotEmpty()) {
                    DetailRow(label = "Network", value = networkNames.joinToString(", "))
                }
                val createdBy = tvShow.createdBy.ifEmpty { tvShow.createdByList.map { it.name }.joinToString(", ") }
                if (createdBy.isNotBlank()) {
                    DetailRow(label = "Created By", value = createdBy)
                }

                // Trailer Section
                if (videos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=${videos.first().key}")
                            )
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.watch_trailer), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Recommendations Section
                if (similar.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.you_may_also_like),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(similar) { simShow ->
                            TvShowCard(tvShow = simShow, onClick = { onTvShowClick(simShow.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF888888),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

