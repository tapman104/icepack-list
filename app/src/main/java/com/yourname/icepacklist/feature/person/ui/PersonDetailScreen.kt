package com.yourname.icepacklist.feature.person.ui

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.Person
import com.yourname.icepacklist.feature.home.domain.PersonImage
import com.yourname.icepacklist.feature.home.domain.PersonMovieCredit
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.core.ui.MovieCard

private const val TMDB_PROFILE_BASE = "https://image.tmdb.org/t/p/w342"
private const val TMDB_POSTER_BASE = "https://image.tmdb.org/t/p/w342"

@Composable
fun PersonDetailScreen(
    viewModel: PersonDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onMovieClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        when (val state = uiState) {
            is PersonDetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFE50914)
                )
            }
            is PersonDetailUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.error_loading_person),
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
            is PersonDetailUiState.Success -> {
                PersonDetailContent(
                    person = state.person,
                    movieCredits = state.movieCredits,
                    images = state.images,
                    onBack = onBack,
                    onMovieClick = onMovieClick
                )
            }
        }
    }
}

@Composable
private fun PersonDetailContent(
    person: Person,
    movieCredits: List<PersonMovieCredit>,
    images: List<PersonImage>,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit
) {
    var expandedBio by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = person.profilePath?.let { "$TMDB_PROFILE_BASE$it" },
                        contentDescription = person.name,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_image_placeholder),
                        error = painterResource(R.drawable.ic_image_placeholder),
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = person.name,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 34.sp
                    )
                    if (!person.knownForDepartment.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = person.knownForDepartment,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                if (!person.birthday.isNullOrBlank() || !person.placeOfBirth.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!person.birthday.isNullOrBlank()) {
                            Text(
                                text = person.birthday,
                                color = Color(0xFF888888),
                                fontSize = 14.sp
                            )
                        }
                        if (!person.birthday.isNullOrBlank() && !person.placeOfBirth.isNullOrBlank()) {
                            Text(text = " • ", color = Color(0xFF888888))
                        }
                        if (!person.placeOfBirth.isNullOrBlank()) {
                            Text(
                                text = person.placeOfBirth,
                                color = Color(0xFF888888),
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (!person.biography.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.person_biography),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = person.biography,
                        color = Color(0xFFBBBBBB),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        maxLines = if (expandedBio) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { expandedBio = !expandedBio }
                    )
                    Text(
                        text = if (expandedBio) "Show less" else "Read more",
                        color = Color(0xFFE50914),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { expandedBio = !expandedBio }
                    )
                }

                if (movieCredits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.person_known_for),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(movieCredits) { credit ->
                            val movie = Movie(
                                id = credit.id,
                                title = credit.title,
                                posterPath = credit.posterPath,
                                backdropPath = null,
                                voteAverage = 0.0,
                                releaseDate = credit.releaseDate,
                                overview = ""
                            )
                            MovieCard(movie = movie, onClick = { onMovieClick(credit.id) })
                        }
                    }
                }
                
                if (images.size > 1) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.person_images),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(images.take(15)) { image ->
                            AsyncImage(
                                model = "$TMDB_PROFILE_BASE${image.filePath}",
                                contentDescription = "Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(200.dp)
                                    .aspectRatio(2f / 3f)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
