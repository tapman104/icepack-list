package com.yourname.icepacklist.feature.watchlist.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.feature.watchlist.domain.WatchlistStatus
import java.util.Locale

private const val TMDB_POSTER_BASE = "https://image.tmdb.org/t/p/w185"

private data class WatchlistTab(
    val title: String,
    val status: WatchlistStatus
)

private val tabs = listOf(
    WatchlistTab("Plan to Watch", WatchlistStatus.PLAN_TO_WATCH),
    WatchlistTab("Watching", WatchlistStatus.WATCHING),
    WatchlistTab("Completed", WatchlistStatus.COMPLETED),
    WatchlistTab("Paused", WatchlistStatus.PAUSED),
    WatchlistTab("Dropped", WatchlistStatus.DROPPED),
    WatchlistTab("Rewatching", WatchlistStatus.REWATCHING)
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
    val itemsFlow = remember(currentTab.status) { viewModel.getByStatus(currentTab.status.name) }
    val itemsList by itemsFlow.collectAsState(initial = emptyList())
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("My List", color = Color.White) },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0.dp),
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D))
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tabs, key = { _, tab -> tab.title }) { index, tab ->
                    WatchlistTabItem(
                        tab = tab,
                        isSelected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
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
                                    if (item.mediaType == MediaType.MOVIE) {
                                        onMovieClick(item.id)
                                    } else {
                                        onTvShowClick(item.id)
                                    }
                                },
                                onRemove = {
                                    viewModel.remove(item.id, item.mediaType)
                                },
                                onUpdateStatus = { newStatus ->
                                    viewModel.updateStatus(item.id, item.mediaType, newStatus.name)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistTabItem(
    tab: WatchlistTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFFE50914) else Color(0xFF1C1C1E))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = tab.title,
            color = if (isSelected) Color.White else Color(0xFF888888),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WatchlistItemRow(
    item: WatchlistEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onUpdateStatus: (WatchlistStatus) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true }
                )
                .padding(12.dp),
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val subText = listOfNotNull(
                    item.year?.takeIf { it.isNotBlank() },
                    item.mediaType.name.uppercase(Locale.US)
                ).joinToString(" • ")
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFFE50914)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val ratingStr = String.format(Locale.US, "%.1f", item.voteAverage)
                Text(
                    text = "★ $ratingStr",
                    style = MaterialTheme.typography.labelMedium,
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
                            color = if (item.status == tab.status.name) Color(0xFFE50914) else Color.White,
                            fontWeight = if (item.status == tab.status.name) FontWeight.Bold else FontWeight.Normal
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
