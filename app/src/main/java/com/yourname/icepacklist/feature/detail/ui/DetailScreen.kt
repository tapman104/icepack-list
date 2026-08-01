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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yourname.icepacklist.core.ui.MovieCard
import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.feature.home.domain.VideoResult
import com.yourname.icepacklist.core.database.WatchStatus
import java.text.SimpleDateFormat
import java.util.Locale

private const val TMDB_BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"
private const val TMDB_PROFILE_BASE = "https://image.tmdb.org/t/p/w185"

@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onMovieClick: (Int) -> Unit = {}
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
            is DetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFE50914)
                )
            }
            is DetailUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ Error loading details",
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
                        onClick = { viewModel.loadMovieDetail() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            is DetailUiState.Success -> {
                DetailContent(
                    movie = state.movie,
                    credits = state.credits,
                    videos = state.videos,
                    similar = state.similar,
                    isInWatchlist = isInWatchlist,
                    watchlistStatus = watchlistStatus,
                    onAddToWatchlist = { viewModel.addToWatchlist(WatchStatus.WATCHING) },
                    onUpdateWatchlistStatus = { viewModel.updateWatchlistStatus(it) },
                    onRemoveFromWatchlist = { viewModel.removeFromWatchlist() },
                    onBack = onBack,
                    onMovieClick = onMovieClick
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    movie: MovieDetail,
    credits: CreditsResponse,
    videos: List<VideoResult>,
    similar: List<Movie>,
    isInWatchlist: Boolean,
    watchlistStatus: WatchStatus?,
    onAddToWatchlist: () -> Unit,
    onUpdateWatchlistStatus: (WatchStatus) -> Unit,
    onRemoveFromWatchlist: () -> Unit,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit
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
                    model = movie.backdropPath?.let { "$TMDB_BACKDROP_BASE$it" },
                    contentDescription = "Backdrop for ${movie.title}",
                    contentScale = ContentScale.Crop,
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
                Text(
                    text = movie.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val ratingStr = String.format(Locale.US, "%.1f", movie.voteAverage)
                    Text(
                        text = "★ $ratingStr",
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    movie.releaseDate?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it.take(4),
                            color = Color(0xFF888888),
                            fontSize = 14.sp
                        )
                    }
                    movie.runtime?.takeIf { it > 0 }?.let {
                        Text(
                            text = "${it / 60}h ${it % 60}m",
                            color = Color(0xFF888888),
                            fontSize = 14.sp
                        )
                    }
                }

                if (movie.genres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        movie.genres.take(3).forEach { genre ->
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
                        Text("+ My List", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { watchlistMenuExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✓ In My List", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = watchlistMenuExpanded,
                            onDismissRequest = { watchlistMenuExpanded = false },
                            modifier = Modifier.background(Color(0xFF2C2C2E))
                        ) {
                            listOf(
                                "Watching" to WatchStatus.WATCHING,
                                "Completed" to WatchStatus.COMPLETED,
                                "Paused" to WatchStatus.PAUSED,
                                "Dropped" to WatchStatus.DROPPED
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
                                text = { Text("Remove from List", color = Color(0xFFFF453A)) },
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
                    text = movie.overview,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                // Cast Section
                if (credits.cast.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Cast",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(credits.cast.take(15)) { person ->
                            Column(
                                modifier = Modifier.width(72.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = person.profilePath?.let { "$TMDB_PROFILE_BASE$it" },
                                    contentDescription = person.name,
                                    contentScale = ContentScale.Crop,
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

                // Director Section
                if (movie.director.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DetailRow(label = "Director", value = movie.director)
                }

                // Details Section
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Details",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Release Date", value = formatDate(movie.releaseDate))
                if (!movie.status.isNullOrBlank()) {
                    DetailRow(label = "Status", value = movie.status)
                }
                if (movie.originCountry.isNotEmpty()) {
                    DetailRow(label = "Country", value = movie.originCountry.joinToString(", "))
                }
                if (!movie.originalLanguage.isNullOrBlank()) {
                    DetailRow(label = "Language", value = movie.originalLanguage.uppercase())
                }
                movie.runtime?.takeIf { it > 0 }?.let {
                    DetailRow(label = "Runtime", value = "${it / 60}h ${it % 60}m")
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
                        Text("Watch Trailer", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Recommendations Section
                if (similar.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "You May Also Like",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(similar) { simMovie ->
                            MovieCard(movie = simMovie, onClick = { onMovieClick(simMovie.id) })
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

private fun formatDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = inputFormat.parse(dateStr)
        if (date != null) outputFormat.format(date) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}
