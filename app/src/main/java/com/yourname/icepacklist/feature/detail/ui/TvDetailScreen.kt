package com.yourname.icepacklist.feature.detail.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.yourname.icepacklist.feature.home.domain.WatchProvider
import com.yourname.icepacklist.feature.home.domain.Keyword
import com.yourname.icepacklist.feature.home.domain.Review
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.feature.watchlist.domain.WatchlistStatus
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
    onPersonClick: (Int) -> Unit = {},
    onSeasonClick: (Int, String) -> Unit = { _, _ -> },
    onFullCastClick: (Int, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val entryState by viewModel.entryState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
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
    onSeasonClick: (Int, String) -> Unit,
    onFullCastClick: (Int, String) -> Unit
) {
    val context = LocalContext.current
    var showMyListSheet by remember { mutableStateOf(false) }

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

                // Watchlist Button Row between genre chips and overview
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (entryState == null) {
                        Button(
                            onClick = {
                                onAddToWatchlist(WatchlistStatus.PLAN_TO_WATCH.name)
                                showMyListSheet = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.add_to_list), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showMyListSheet = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.in_my_list), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = tvShow.overview,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                // Cast Section — circular cards LazyRow, max 15
                if (credits.cast.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.cast),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "View All",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .clickable {
                                    com.yourname.icepacklist.feature.detail.ui.CreditsHolder.credits = credits
                                    onFullCastClick(tvShow.id, "tv")
                                }
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(modifier = Modifier.height(92.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(credits.cast.take(15), key = { it.id }) { person ->
                            CastItemCard(
                                person = person,
                                onClick = { onPersonClick(person.id) }
                            )
                        }
                    }
                }

                // Crew Section
                val crewJobs = listOf("Director", "Writer", "Screenplay", "Creator", "Executive Producer")
                val crewList = credits.crew.filter { it.job in crewJobs }.take(6)
                if (crewList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Crew",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(modifier = Modifier.height(92.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(crewList, key = { it.id.toString() + it.job }) { person ->
                            CrewItemCard(
                                person = person,
                                onClick = { onPersonClick(person.id) }
                            )
                        }
                    }
                }


                // Details Section
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.details),
                    color = MaterialTheme.colorScheme.onSurface,
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
                tvShow.numberOfSeasons?.let { seasons ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeasonClick(tvShow.id, tvShow.name) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Seasons",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(
                                text = "$seasons",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "View Seasons", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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

                // Keywords Section
                if (keywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Keywords",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        keywords.take(8).forEach { keyword ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Text(
                                    text = keyword.name,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
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
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(modifier = Modifier.height(180.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(similar, key = { it.id }) { simShow ->
                            TvShowCard(tvShow = simShow, onClick = { onTvShowClick(simShow.id) })
                        }
                    }
                }
                // Reviews Section
                if (reviews.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Reviews",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        reviews.take(3).forEach { review ->
                            ReviewItem(review = review)
                        }
                    }
                }

            }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CastItemCard(person: com.yourname.icepacklist.feature.home.domain.Cast, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = person.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = person.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(com.yourname.icepacklist.R.drawable.ic_image_placeholder),
            error = painterResource(com.yourname.icepacklist.R.drawable.ic_image_placeholder),
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = person.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = person.character ?: "",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}



@Composable
private fun CrewItemCard(person: com.yourname.icepacklist.feature.home.domain.Crew, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = person.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = person.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(com.yourname.icepacklist.R.drawable.ic_image_placeholder),
            error = painterResource(com.yourname.icepacklist.R.drawable.ic_image_placeholder),
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = person.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = person.job ?: "",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReviewItem(review: com.yourname.icepacklist.feature.home.domain.Review) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val avatarPath = review.authorDetails?.avatarPath
            if (avatarPath != null) {
                val imageUrl = if (avatarPath.startsWith("/http")) avatarPath.substring(1) else "https://image.tmdb.org/t/p/w185$avatarPath"
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.author.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = review.author,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                review.authorDetails?.rating?.let { rating ->
                    Text(
                        text = "★ $rating",
                        color = Color(0xFFFFC107),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = review.content,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis
        )
        if (!expanded && review.content.length > 150) {
            Text(
                text = "Read More",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { expanded = true }
            )
        }
    }
}
