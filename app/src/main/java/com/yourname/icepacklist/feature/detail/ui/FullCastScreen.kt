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

    Scaffold(
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
                items(credits.cast, key = { "cast_${it.id}_${it.character}" }) { person ->
                    FullCastItem(person, onClick = { onPersonClick(person.id) })
                }
            }
            
            val crewJobs = listOf("Director", "Writer", "Screenplay", "Creator", "Executive Producer")
            val crewList = credits.crew.filter { it.job in crewJobs }
            if (crewList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Crew", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                items(crewList, key = { "crew_${it.id}_${it.job}" }) { person ->
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
