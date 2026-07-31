package com.yourname.icepacklist.feature.detail.ui

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
import coil.compose.AsyncImage
import com.yourname.icepacklist.core.ui.UiState
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
import com.yourname.icepacklist.feature.home.domain.CreditsResponse

private const val TMDB_BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"
private const val TMDB_PROFILE_BASE = "https://image.tmdb.org/t/p/w185"

@Composable
fun TvDetailScreen(
    viewModel: TvDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val tvState by viewModel.tvShowState.collectAsState()
    val creditsState by viewModel.creditsState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))) {
        when (val state = tvState) {
            is UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFE50914))
            }
            is UiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠\uFE0F Error loading details", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.message, color = Color(0xFF888888), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadData() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            is UiState.Success -> {
                TvDetailContent(tvShow = state.data, creditsState = creditsState, onBack = onBack)
            }
        }
    }
}

@Composable
private fun TvDetailContent(tvShow: TvShowDetail, creditsState: UiState<CreditsResponse>, onBack: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                AsyncImage(
                    model = tvShow.backdropPath?.let { "$TMDB_BACKDROP_BASE$it" },
                    contentDescription = "Backdrop for ${tvShow.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(100.dp)
                        .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xFF0D0D0D))))
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
                    text = tvShow.name,
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
                    Text(
                        text = "★ ",
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    tvShow.firstAirDate?.takeIf { it.length >= 4 }?.let {
                        Text(text = it.take(4), color = Color(0xFF888888), fontSize = 14.sp)
                    }
                    tvShow.numberOfSeasons?.let {
                        Text(text = "${it} Seasons", color = Color(0xFF888888), fontSize = 14.sp)
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

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = tvShow.overview,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Cast",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                when (creditsState) {
                    is UiState.Success -> {
                        val cast = creditsState.data.cast.take(15)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(cast) { person ->
                                Column(modifier = Modifier.width(100.dp)) {
                                    AsyncImage(
                                        model = person.profilePath?.let { "$TMDB_PROFILE_BASE$it" },
                                        contentDescription = person.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().aspectRatio(2f/3f).clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = person.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = person.character ?: "",
                                        color = Color(0xFF888888),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp), color = Color(0xFFE50914))
                    }
                    else -> {}
                }
            }
        }
    }
}
