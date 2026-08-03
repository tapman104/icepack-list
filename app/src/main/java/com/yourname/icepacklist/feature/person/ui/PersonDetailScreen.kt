package com.yourname.icepacklist.feature.person.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.PersonDetail
import com.yourname.icepacklist.feature.home.domain.PersonImage
import com.yourname.icepacklist.feature.home.domain.CombinedCreditsCast
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.core.ui.MovieCard

private const val TMDB_PROFILE_BASE = "https://image.tmdb.org/t/p/w342"
private const val TMDB_POSTER_BASE = "https://image.tmdb.org/t/p/w342"

@Composable
fun PersonDetailScreen(
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onTvClick: (Int) -> Unit,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFE50914)
                )
            }
            uiState.isError -> {
                OfflineErrorContent(
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.person != null -> {
                PersonContent(
                    uiState = uiState,
                    onBack = onBack,
                    onMovieClick = onMovieClick,
                    onTvClick = onTvClick
                )
            }
        }
    }
}

@Composable
private fun OfflineErrorContent(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.error_loading_person),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914))
        ) {
            Text(stringResource(R.string.retry), color = Color.White)
        }
    }
}

@Composable
private fun PersonContent(
    uiState: PersonUiState,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    onTvClick: (Int) -> Unit,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {
    val person = uiState.person!!
    val scrollState = rememberLazyListState()
    val sectionExpanded = remember {
        androidx.compose.runtime.mutableStateMapOf(
            "drama" to true,
            "movie" to true,
            "tv" to true,
            "images" to true
        )
    }

    LazyColumn(
        state = scrollState,
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // ── Hero ──────────────────────────────────────────────
        item(key = "hero") {
            PersonHero(person = person, onBack = onBack)
        }

        // ── Personal Info ─────────────────────────────────────
        item(key = "info") {
            PersonInfoSection(
                person = person,
                nativeName = uiState.nativeName,
                nationality = uiState.nationality,
                age = uiState.age
            )
        }

        // ── Biography ─────────────────────────────────────────
        if (!person.biography.isNullOrBlank()) {
            item(key = "bio") {
                PersonBiographySection(biography = person.biography)
            }
        }

        // ── Known For (horizontal) ────────────────────────────
        if (uiState.knownFor.isNotEmpty()) {
            item(key = "known_for_header") {
                SectionHeader(title = "Known For")
            }
            item(key = "known_for_row") {
                KnownForRow(
                    items = uiState.knownFor,
                    onMovieClick = onMovieClick,
                    onTvClick = onTvClick
                )
            }
        }

        // ── Drama (vertical) ─────────────────────────────────
        if (uiState.dramas.isNotEmpty()) {
            val expanded = sectionExpanded["drama"] == true
            item(key = "drama_header") {
                SectionHeader(
                    title = "Drama",
                    count = uiState.dramas.size,
                    expanded = expanded,
                    onToggle = { sectionExpanded["drama"] = !expanded }
                )
            }
            if (expanded) {
                items(
                    items = uiState.dramas,
                    key = { "drama_${it.id}" }
                ) { credit ->
                    FilmographyCard(
                        credit = credit,
                        onClick = { onTvClick(credit.id) },
                        onAddToWatchlist = { viewModel.addToWatchlist(credit) }
                    )
                }
            }
        }

        // ── Movie (vertical) ──────────────────────────────────
        if (uiState.movies.isNotEmpty()) {
            val expanded = sectionExpanded["movie"] == true
            item(key = "movie_header") {
                SectionHeader(
                    title = "Movie",
                    count = uiState.movies.size,
                    expanded = expanded,
                    onToggle = { sectionExpanded["movie"] = !expanded }
                )
            }
            if (expanded) {
                items(
                    items = uiState.movies,
                    key = { "movie_${it.id}" }
                ) { credit ->
                    FilmographyCard(
                        credit = credit,
                        onClick = { onMovieClick(credit.id) },
                        onAddToWatchlist = { viewModel.addToWatchlist(credit) }
                    )
                }
            }
        }

        // ── TV Show (vertical) ────────────────────────────────
        if (uiState.tvShows.isNotEmpty()) {
            val expanded = sectionExpanded["tv"] == true
            item(key = "tv_header") {
                SectionHeader(
                    title = "TV Show",
                    count = uiState.tvShows.size,
                    expanded = expanded,
                    onToggle = { sectionExpanded["tv"] = !expanded }
                )
            }
            if (expanded) {
                items(
                    items = uiState.tvShows,
                    key = { "tv_${it.id}" }
                ) { credit ->
                    FilmographyCard(
                        credit = credit,
                        onClick = { onTvClick(credit.id) },
                        onAddToWatchlist = { viewModel.addToWatchlist(credit) }
                    )
                }
            }
        }

        // ── Images (horizontal) ───────────────────────────────
        if (uiState.images.isNotEmpty()) {
            val expanded = sectionExpanded["images"] == true
            item(key = "images_header") {
                SectionHeader(
                    title = "Images",
                    expanded = expanded,
                    onToggle = { sectionExpanded["images"] = !expanded }
                )
            }
            if (expanded) {
                item(key = "images_row") {
                    PersonImagesRow(images = uiState.images)
                }
            }
        }
    }
}

// ── FilmographyCard ──────────────────────────────────────────
@Composable
private fun FilmographyCard(
    credit: CombinedCreditsCast,
    onClick: () -> Unit,
    onAddToWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    if (credit.posterPath != null)
                        "https://image.tmdb.org/t/p/w185${credit.posterPath}"
                    else null
                )
                .size(160, 240)
                .crossfade(true)
                .build(),
            contentDescription = credit.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 80.dp, height = 88.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title + metadata
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = credit.displayTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildString {
                    if (credit.mediaType == "movie") append("Korean Movie")
                    else append("Korean TV Show")
                    if (credit.displayYear.isNotEmpty()) append(" • ${credit.displayYear}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (credit.displayCharacter.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = credit.displayCharacter,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (credit.episodeCount != null && credit.mediaType == "tv") {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "(Ep. ${credit.episodeCount})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // + button
        IconButton(onClick = onAddToWatchlist) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add to watchlist",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── SectionHeader ─────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title: String,
    count: Int? = null,
    expanded: Boolean = true,
    onToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (count != null) "$title" else title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        if (count != null || title == "Images") {
            Icon(
                imageVector = if (expanded)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Extracted Components from Original Screen ────────────────

@Composable
private fun PersonHero(person: PersonDetail, onBack: () -> Unit) {
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

@Composable
private fun PersonInfoSection(
    person: PersonDetail,
    nativeName: String,
    nationality: String,
    age: Int?,
    modifier: Modifier = Modifier
) {
    // Split name into first and family
    val nameParts = person.name.trim().split(" ")
    val firstName = nameParts.firstOrNull() ?: ""
    val familyName = nameParts.drop(1).joinToString(" ")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        PersonInfoRow(
            icon = Icons.Outlined.AccountCircle,
            label = "First Name",
            value = firstName
        )
        PersonInfoRow(
            icon = Icons.Outlined.People,
            label = "Family Name",
            value = familyName
        )
        if (nativeName.isNotEmpty()) {
            PersonInfoRow(
                icon = Icons.Outlined.Translate,
                label = "Native Name",
                value = nativeName
            )
        }
        if (nationality.isNotEmpty()) {
            PersonInfoRow(
                icon = Icons.Outlined.Public,
                label = "Nationality",
                value = nationality
            )
        }
        if (!person.birthday.isNullOrBlank()) {
            PersonInfoRow(
                icon = Icons.Outlined.CalendarToday,
                label = "Birthday",
                value = person.birthday
            )
        }
        if (age != null) {
            PersonInfoRow(
                icon = Icons.Outlined.Info,
                label = "Age",
                value = age.toString()
            )
        }
    }
}

@Composable
private fun PersonInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PersonBiographySection(biography: String) {
    var expandedBio by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.person_biography),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = biography,
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
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun KnownForRow(
    items: List<CombinedCreditsCast>,
    onMovieClick: (Int) -> Unit,
    onTvClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(items, key = { it.id }) { credit ->
            val movie = Movie(
                id = credit.id,
                title = credit.title ?: credit.name ?: "",
                posterPath = credit.posterPath,
                backdropPath = null,
                voteAverage = 0.0,
                releaseDate = credit.releaseDate ?: credit.firstAirDate,
                overview = ""
            )
            MovieCard(
                movie = movie,
                onClick = {
                    if (credit.mediaType == "movie") onMovieClick(credit.id)
                    else onTvClick(credit.id)
                }
            )
        }
    }
}

@Composable
private fun PersonImagesRow(images: List<PersonImage>) {
    LazyRow(
        modifier = Modifier.height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(images.take(15), key = { it.filePath }) { image ->
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
