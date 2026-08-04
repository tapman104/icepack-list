package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.feature.watchlist.domain.WatchlistStatus
import java.util.Locale

private const val TMDB_BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"

@Composable
fun DetailHeroSection(
    movie: MovieDetail,
    entryState: WatchlistEntity?,
    onBack: () -> Unit,
    onHide: () -> Unit,
    onAddToWatchlist: (String) -> Unit,
    onShowMyListSheet: () -> Unit
) {
    val context = LocalContext.current
    val overlayColor = remember { Color.Black.copy(alpha = 0.5f) }
    val ratingColor = remember { Color(0xFFFFC107) }
    val mutedGray = remember { Color(0xFF888888) }
    val primaryRed = remember { Color(0xFFE50914) }

    // M12 — String.format in remember, only recalculates when voteAverage changes
    val ratingStr = remember(movie.voteAverage) {
        String.format(Locale.US, "%.1f", movie.voteAverage)
    }

    // H1 — Gradient brush captured in remember; theme color captured first as a local val
    val backgroundColor = MaterialTheme.colorScheme.background
    val heroGradient = remember(backgroundColor) {
        Brush.verticalGradient(listOf(Color.Transparent, backgroundColor))
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            // H3 — backdrop AsyncImage with explicit size so Coil does not decode at full resolution
            val backdropUrl = movie.backdropPath?.let { "$TMDB_BACKDROP_BASE$it" }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(backdropUrl)
                    .size(780, 440)
                    .crossfade(true)
                    .build(),
                contentDescription = "Backdrop for ${movie.title}",
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
                    .background(heroGradient) // H1 — remembered brush
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(overlayColor, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(
                    onClick = onHide,
                    modifier = Modifier.background(overlayColor, CircleShape)
                ) {
                    Icon(Icons.Outlined.VisibilityOff, contentDescription = "Hide", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                movie.posterPath?.let {
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
                        text = movie.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 34.sp
                    )
                    if (!movie.tagline.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = movie.tagline,
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
                // M12 — using pre-computed ratingStr from remember above
                Text(
                    text = "★ $ratingStr",
                    color = ratingColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                movie.releaseDate?.takeIf { it.isNotEmpty() }?.let {
                    Text(
                        text = it.take(4),
                        color = mutedGray,
                        fontSize = 14.sp
                    )
                }
                movie.runtime?.takeIf { it > 0 }?.let {
                    Text(
                        text = "${it / 60}h ${it % 60}m",
                        color = mutedGray,
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
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Text(
                                text = genre.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                if (entryState == null) {
                    Button(
                        onClick = {
                            onAddToWatchlist(WatchlistStatus.PLAN_TO_WATCH.name)
                            onShowMyListSheet()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.add_to_list), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onShowMyListSheet,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.in_my_list), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
