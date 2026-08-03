package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.Cast
import com.yourname.icepacklist.feature.home.domain.Crew

// H12 — crewJobs constant at top level; never allocated inside the composable or LazyColumn DSL
private val CREW_JOBS = setOf("Director", "Writer", "Screenplay", "Creator", "Executive Producer")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullCastScreen(
    mediaId: Int,
    mediaType: String,
    onBack: () -> Unit = {},
    onPersonClick: (Int) -> Unit = {}
) {
    val credits = CreditsHolder.credits

    DisposableEffect(Unit) {
        onDispose {
            CreditsHolder.credits = null
        }
    }

    if (credits == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // H12 — crewList computed in remember above the LazyColumn; no computation inside the DSL block
    val crewList = remember(credits) {
        credits.crew.filter { it.job in CREW_JOBS }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text("Cast & Crew", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (credits.cast.isNotEmpty()) {
                item {
                    Text("Cast", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                // M3 — Cast id is unique; plain Int key, no string template allocation
                items(credits.cast, key = { it.id }) { person ->
                    FullCastItem(person, onClick = { onPersonClick(person.id) })
                }
            }

            // H12 — crewList is pre-computed above; no filter or listOf inside the DSL
            if (crewList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Crew", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                // M4 — compound Int key for crew (same person can have two jobs); no string allocation
                items(crewList, key = { it.id * 31 + (it.job?.hashCode() ?: 0) }) { person ->
                    FullCrewItem(person, onClick = { onPersonClick(person.id) })
                }
            }
        }
    }
}

@Composable
fun FullCastItem(person: Cast, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = person.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = person.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_image_placeholder),
            error = painterResource(R.drawable.ic_image_placeholder),
            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.DarkGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(person.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            if (person.character.isNotBlank()) {
                Text(person.character, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FullCrewItem(person: Crew, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = person.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = person.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_image_placeholder),
            error = painterResource(R.drawable.ic_image_placeholder),
            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.DarkGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(person.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            person.job?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
