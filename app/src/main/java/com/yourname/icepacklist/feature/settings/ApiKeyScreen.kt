package com.yourname.icepacklist.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.yourname.icepacklist.core.datastore.ContentFilter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ApiKeyScreen(
    viewModel: ApiKeyViewModel = hiltViewModel()
) {
    val savedKey by viewModel.savedKey.collectAsState()
    var inputKey by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }

    // Pre-fill field if a key is already saved
    LaunchedEffect(savedKey) {
        if (inputKey.isEmpty() && savedKey != null) {
            inputKey = savedKey!!
        }
    }

    val context = LocalContext.current
    val backupState by viewModel.backupState.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: App Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "App Settings", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter your TMDB v3 API key. Get one free at themoviedb.org.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("API Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (keyVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = if (keyVisible) "Hide key" else "Show key"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { if (inputKey.isNotBlank()) viewModel.saveKey(inputKey) }
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveKey(inputKey) },
                            enabled = inputKey.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Key")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.clearKey()
                                inputKey = ""
                            },
                            enabled = savedKey != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }
                    }

                    if (savedKey != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Key saved",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setDarkTheme(!isDarkTheme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Dark Theme", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Switch(checked = isDarkTheme, onCheckedChange = null)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Data & Backup",
                        style = MaterialTheme.typography.titleMedium
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
                        val progress by viewModel.importProgress.collectAsState()
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    } else if (backupState == BackupState.Loading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }

            // Section 2: Home Feed Content
            val homeContentFilter by viewModel.homeContentFilter.collectAsState()
            ContentRegionPicker(
                title = "Home Region",
                description = "Filter the Trending, Popular, Now Playing, and Top Rated rows by country. Selecting ALL disables filtering.",
                selectedFilters = homeContentFilter,
                onSelect = { viewModel.setHomeContentFilter(it) }
            )

            // Section 3: Search Results Content
            val searchContentFilter by viewModel.searchContentFilter.collectAsState()
            ContentRegionPicker(
                title = "Search Region",
                description = "Filter your manual search queries by country. Selecting ALL disables filtering.",
                selectedFilters = searchContentFilter,
                onSelect = { viewModel.setSearchContentFilter(it) }
            )

            // Section 4: Recommendations Content
            val recommendationsContentFilter by viewModel.recommendationsContentFilter.collectAsState()
            ContentRegionPicker(
                title = "Recommendations Region",
                description = "Filter the recommendations row by country. Selecting ALL turns the recommendations row off entirely.",
                selectedFilters = recommendationsContentFilter,
                onSelect = { viewModel.setRecommendationsContentFilter(it) }
            )

            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav if needed
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // "Select All Regions" checkbox at the top
            FilterCheckRow(
                label = ContentFilter.ALL.displayName,
                checked = selectedFilters.contains(ContentFilter.ALL),
                onToggle = {
                    // Tapping ALL clears all specific selections and selects only ALL
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

/**
 * Toggle a specific (non-ALL) filter in the set.
 * - If ALL is tapped: handled separately (select only ALL).
 * - If a specific filter is tapped: remove ALL from the set and toggle that entry.
 * - If the resulting set is empty: fall back to setOf(ALL).
 */
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
