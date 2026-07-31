package com.yourname.icepacklist.feature.watchlist.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yourname.icepacklist.core.database.WatchlistEntity
import java.util.Locale

private const val TMDB_POSTER_BASE = "https://image.tmdb.org/t/p/w185"

private data class WatchlistTab(
    val title: String,
    val status: String
)

private val tabs = listOf(
    WatchlistTab("Watching", "watching"),
    WatchlistTab("Completed", "completed"),
    WatchlistTab("Paused", "paused"),
    WatchlistTab("Dropped", "dropped")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit = {},
    onTvShowClick: (Int) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentTab = tabs[selectedTabIndex]
    val itemsFlow = remember(currentTab.status) { viewModel.getByStatus(currentTab.status) }
    val itemsList by itemsFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My List", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D)
                )
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D))
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFFE50914)
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title) },
                        selectedContentColor = Color(0xFFE50914),
                        unselectedContentColor = Color(0xFF888888)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (itemsList.isEmpty()) {
                    Text(
                        text = "Nothing here yet",
                        color = Color(0xFF888888),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(itemsList, key = { "${it.mediaType}_${it.id}" }) { item ->
                            WatchlistItemRow(
                                item = item,
                                onClick = {
                                    if (item.mediaType == "movie") {
                                        onMovieClick(item.id)
                                    } else {
                                        onTvShowClick(item.id)
                                    }
                                },
                                onRemove = {
                                    viewModel.remove(item.id, item.mediaType)
                                },
                                onUpdateStatus = { newStatus ->
                                    viewModel.updateStatus(item.id, item.mediaType, newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WatchlistItemRow(
    item: WatchlistEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1C1C1E))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true }
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.posterPath?.let { "$TMDB_POSTER_BASE$it" },
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(56.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val subText = listOfNotNull(
                    item.year?.takeIf { it.isNotBlank() },
                    item.mediaType.uppercase(Locale.US)
                ).joinToString(" • ")
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val ratingStr = String.format(Locale.US, "%.1f", item.voteAverage)
                Text(
                    text = "★ $ratingStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFC107)
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove from list",
                    tint = Color(0xFF888888)
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(Color(0xFF2C2C2E))
        ) {
            tabs.forEach { tab ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = tab.title,
                            color = if (item.status == tab.status) Color(0xFFE50914) else Color.White,
                            fontWeight = if (item.status == tab.status) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onUpdateStatus(tab.status)
                    }
                )
            }
        }
    }
}
