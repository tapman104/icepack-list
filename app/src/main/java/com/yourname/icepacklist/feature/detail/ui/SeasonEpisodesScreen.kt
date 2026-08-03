package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.Episode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonEpisodesScreen(
    viewModel: SeasonEpisodesViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val totalSeasons by viewModel.totalSeasons.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.tvName, color = MaterialTheme.colorScheme.onSurface) },
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            var expanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.padding(16.dp)) {
                // M9 — single safe-cast instead of is-check + explicit as-cast
                val successState = uiState as? SeasonEpisodesUiState.Success
                val episodeCount = successState?.seasonData?.episodes?.size ?: 0
                val epsText = if (episodeCount > 0) " ($episodeCount eps)" else ""

                OutlinedButton(onClick = { expanded = true }) {
                    Text("Season $selectedSeason$epsText", color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    for (i in 1..totalSeasons) {
                        DropdownMenuItem(
                            text = { Text("Season $i") },
                            onClick = {
                                viewModel.loadSeason(i)
                                expanded = false
                            }
                        )
                    }
                }
            }

            when (val state = uiState) {
                is SeasonEpisodesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE50914))
                    }
                }
                is SeasonEpisodesUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                is SeasonEpisodesUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.seasonData.episodes, key = { it.id }) { episode ->
                            EpisodeItem(episode)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EpisodeItem(episode: Episode) {
    var showPlot by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // L6 — still image URL computed in remember; no string allocation on every recomposition
    val stillUrl = remember(episode.stillPath) {
        episode.stillPath?.let { "https://image.tmdb.org/t/p/w300$it" }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row {
            // L6 — size(240, 135) added; Coil decodes to display dimensions (120dp×68dp at 2x)
            // instead of full w300 resolution
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(stillUrl)
                    .size(240, 135)
                    .crossfade(true)
                    .build(),
                contentDescription = episode.name,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_image_placeholder),
                error = painterResource(R.drawable.ic_image_placeholder),
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                val epNum = episode.episodeNumber
                val runtime = episode.runtime?.let { "$it m" } ?: ""
                Text(
                    text = "Ep $epNum" + if (runtime.isNotBlank()) " • $runtime" else "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = episode.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                episode.airDate?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        if (episode.overview.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (showPlot) "Hide Plot" else "Show Plot",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { showPlot = !showPlot }.padding(vertical = 4.dp)
            )
            // L5 — plot text extracted into its own composable so toggling showPlot only
            // recomposes EpisodePlot, not the AsyncImage above
            EpisodePlot(plot = episode.overview, showPlot = showPlot)
        }
    }
}

// L5 — separate composable for the plot text; AsyncImage in EpisodeItem is outside this
// recomposition scope and will not recompose when showPlot toggles
@Composable
private fun EpisodePlot(plot: String, showPlot: Boolean) {
    if (showPlot) {
        Text(
            text = plot,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
