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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast
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
                actions = {
                    val context = LocalContext.current
                    IconButton(onClick = { Toast.makeText(context, "Import feature coming soon", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Import", tint = Color.White)
                    }
                    IconButton(onClick = { Toast.makeText(context, "Export feature coming soon", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Export", tint = Color.White)
                    }
                },
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
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
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
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1C1C1E), Color(0xFF28282B))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
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
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE50914).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.mediaType.name.uppercase(Locale.US),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF5252)
                        )
                    }
                    
                    if (!item.year.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.year,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val ratingStr = String.format(Locale.US, "%.1f", item.voteAverage)
                    Text(
                        text = ratingStr,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove from list",
                    tint = Color(0xFFFF5252)
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
