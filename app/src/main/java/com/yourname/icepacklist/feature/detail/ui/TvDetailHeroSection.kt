package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.feature.watchlist.domain.WatchlistStatus
import java.util.Locale

private const val TMDB_BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"

@Composable
fun TvDetailHeroSection(
    tvShow: TvShowDetail,
    entryState: WatchlistEntity?,
    onBack: () -> Unit,
    onAddToWatchlist: (String) -> Unit,
    onShowMyListSheet: () -> Unit
) {
    Column {
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
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

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
                        color = MaterialTheme.colorScheme.onSurface,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
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
