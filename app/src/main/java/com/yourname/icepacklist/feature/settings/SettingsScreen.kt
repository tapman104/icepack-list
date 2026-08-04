package com.yourname.icepacklist.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import com.yourname.icepacklist.core.database.entity.HiddenItemEntity
import com.yourname.icepacklist.core.datastore.ContentFilter
import com.yourname.icepacklist.core.datastore.ThemeMode
import kotlinx.coroutines.launch
import com.yourname.icepacklist.BuildConfig
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToApiKey: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val hiddenItems by viewModel.hiddenItems.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(backupState) {
        when (val state = backupState) {
            is BackupState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetBackupState()
            }
            is BackupState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetBackupState()
            }
            else -> {}
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it, context.contentResolver) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(it, context.contentResolver) }
    }

    var expandedSection by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                SettingsSectionTitle("Appearance")
                SettingsThemeSelector(
                    currentTheme = uiState.themeMode,
                    onThemeSelected = viewModel::setThemeMode
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SettingsSectionTitle("API Key")
                SettingsRow(
                    title = "Current Key",
                    subtitle = uiState.apiKeyMasked,
                    action = {
                        OutlinedButton(onClick = onNavigateToApiKey) {
                            Text("Change Key")
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SettingsSectionTitle("Content")
                
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExpandableSettingRow(
                            title = "Adult Content",
                            isExpanded = expandedSection == "adult",
                            onClick = { expandedSection = if (expandedSection == "adult") null else "adult" }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Include adult content in results", modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = uiState.adultContentEnabled,
                                        onCheckedChange = viewModel::setAdultContentEnabled
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Hides explicit adult titles. 18+ rated dramas and movies are unaffected.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        ExpandableSettingRow(
                            title = "Home Region",
                            isExpanded = expandedSection == "home",
                            onClick = { expandedSection = if (expandedSection == "home") null else "home" }
                        ) {
                            ContentRegionPicker(
                                title = "Home Region",
                                description = "Filter the Trending, Popular, Now Playing, and Top Rated rows by country. Selecting ALL disables filtering.",
                                selectedFilters = uiState.homeContentFilter,
                                onSelect = viewModel::setHomeContentFilter
                            )
                        }

                        ExpandableSettingRow(
                            title = "Search Region",
                            isExpanded = expandedSection == "search",
                            onClick = { expandedSection = if (expandedSection == "search") null else "search" }
                        ) {
                            ContentRegionPicker(
                                title = "Search Region",
                                description = "Filter your manual search queries by country. Selecting ALL disables filtering.",
                                selectedFilters = uiState.searchContentFilter,
                                onSelect = viewModel::setSearchContentFilter
                            )
                        }

                        ExpandableSettingRow(
                            title = "Recommendations Region",
                            isExpanded = expandedSection == "recs",
                            onClick = { expandedSection = if (expandedSection == "recs") null else "recs" }
                        ) {
                            ContentRegionPicker(
                                title = "Recommendations Region",
                                description = "Filter the recommendations row by country. Selecting ALL turns the recommendations row off entirely.",
                                selectedFilters = uiState.recommendationsContentFilter,
                                onSelect = viewModel::setRecommendationsContentFilter
                            )
                        }
                        ExpandableSettingRow(
                            title = "Hidden Titles",
                            isExpanded = expandedSection == "hidden",
                            onClick = { expandedSection = if (expandedSection == "hidden") null else "hidden" }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (hiddenItems.isEmpty()) {
                                    Text("No titles are hidden.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    hiddenItems.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { viewModel.unhideItem(item.id, item.mediaType) }) {
                                                Text("Restore")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Cache & Storage
            item {
                SettingsSectionTitle("Cache & Storage")
                SettingsRow(
                    title = "Clear Cache",
                    subtitle = "Free up space by clearing cached images",
                    action = {
                        IconButton(onClick = {
                            context.imageLoader.diskCache?.clear()
                            context.imageLoader.memoryCache?.clear()
                            context.cacheDir.deleteRecursively()
                            scope.launch {
                                snackbarHostState.showSnackbar("Cache cleared")
                            }
                        }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Clear Cache")
                        }
                    }
                )
                
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Data & Backup",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Export your watchlist to a JSON file, or import an existing backup. Importing will merge the data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                exportLauncher.launch("icepack_watchlist_backup_$dateStr.json")
                            },
                            modifier = Modifier.weight(1f),
                            enabled = backupState != BackupState.Loading && !isImporting
                        ) {
                            Text("Export Backup")
                        }

                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f),
                            enabled = backupState != BackupState.Loading && !isImporting
                        ) {
                            Text("Import Backup")
                        }
                    }

                    if (isImporting) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val progress by viewModel.importProgress.collectAsStateWithLifecycle()
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    } else if (backupState == BackupState.Loading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // About
            item {
                SettingsSectionTitle("About")
                SettingsRow(
                    title = "App Version",
                    subtitle = BuildConfig.VERSION_NAME
                )
                SettingsRow(
                    title = "View on GitHub",
                    subtitle = "Check out the source code",
                    modifier = Modifier.clickable { }
                )
                SettingsRow(
                    title = "Rate the App",
                    subtitle = "Leave a review on the Play Store",
                    modifier = Modifier.clickable { }
                )
            }
        }
    }
}

@Composable
fun ExpandableSettingRow(
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                
                val rotation by animateFloatAsState(targetValue = if (isExpanded) 90f else 0f)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotation)
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = action == null) { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (action != null) {
            Spacer(modifier = Modifier.width(16.dp))
            action()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsThemeSelector(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val options = listOf(ThemeMode.SYSTEM to "Follow System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark")
    
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        options.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onThemeSelected(mode) },
                selected = mode == currentTheme
            ) {
                Text(label)
            }
        }
    }
}

@Composable
fun ContentRegionPicker(
    title: String,
    description: String,
    selectedFilters: Set<ContentFilter>,
    onSelect: (Set<ContentFilter>) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            FilterCheckRow(
                label = ContentFilter.ALL.displayName,
                checked = selectedFilters.contains(ContentFilter.ALL),
                onToggle = {
                    onSelect(setOf(ContentFilter.ALL))
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            val asianDramas = listOf(ContentFilter.K_DRAMA, ContentFilter.J_DRAMA, ContentFilter.C_DRAMA, ContentFilter.ANIME, ContentFilter.THAI_DRAMA, ContentFilter.INDIAN)
            val western = listOf(ContentFilter.US, ContentFilter.UK)

            Text(text = "Asian Drama", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            asianDramas.forEach { filter ->
                FilterCheckRow(
                    label = filter.displayName,
                    checked = filter in selectedFilters,
                    onToggle = { onSelect(toggleFilter(selectedFilters, filter)) }
                )
            }

            Text(text = "Western", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 16.dp))
            western.forEach { filter ->
                FilterCheckRow(
                    label = filter.displayName,
                    checked = filter in selectedFilters,
                    onToggle = { onSelect(toggleFilter(selectedFilters, filter)) }
                )
            }
        }
    }
}

private fun toggleFilter(current: Set<ContentFilter>, filter: ContentFilter): Set<ContentFilter> {
    val withoutAll = current - ContentFilter.ALL
    val updated = if (filter in withoutAll) {
        withoutAll - filter
    } else {
        withoutAll + filter
    }
    return if (updated.isEmpty()) setOf(ContentFilter.ALL) else updated
}

@Composable
private fun FilterCheckRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
