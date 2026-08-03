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
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
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
            Text(
                text = "TMDB API Key",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Enter your TMDB v3 API key. Get one free at themoviedb.org.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                Text(
                    text = "✓ Key saved",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setDarkTheme(!isDarkTheme) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dark Theme", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = isDarkTheme, onCheckedChange = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            val recommendationsEnabled by viewModel.recommendationsEnabled.collectAsState()
            val searchFilterEnabled by viewModel.searchFilterEnabled.collectAsState()

            Text(
                text = "Discovery",
                style = MaterialTheme.typography.titleLarge
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setRecommendationsEnabled(!recommendationsEnabled) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Show Recommendations", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = recommendationsEnabled, onCheckedChange = null)
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setSearchFilterEnabled(!searchFilterEnabled) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Filter Search by Region", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Applies your Content Region to search results", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = searchFilterEnabled, onCheckedChange = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Content Region",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Filter the home screen to show content from specific regions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val contentFilter by viewModel.contentFilter.collectAsState()
            
            val asianDramas = listOf(ContentFilter.K_DRAMA, ContentFilter.J_DRAMA, ContentFilter.C_DRAMA, ContentFilter.ANIME, ContentFilter.THAI_DRAMA, ContentFilter.INDIAN)
            val western = listOf(ContentFilter.US, ContentFilter.UK)
            val allRegions = listOf(ContentFilter.ALL)

            Text(text = "Asian Drama", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            asianDramas.forEach { filter ->
                FilterRow(filter, filter == contentFilter) { viewModel.setContentFilter(filter) }
            }

            Text(text = "Western", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            western.forEach { filter ->
                FilterRow(filter, filter == contentFilter) { viewModel.setContentFilter(filter) }
            }

            Text(text = "All Regions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            allRegions.forEach { filter ->
                FilterRow(filter, filter == contentFilter) { viewModel.setContentFilter(filter) }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Data & Backup",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Export your watchlist to a JSON file, or import an existing backup. Importing will merge the data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                val progress by viewModel.importProgress.collectAsState()
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            } else if (backupState == BackupState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav if needed
        }
    }
}

@Composable
private fun FilterRow(filter: ContentFilter, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = filter.displayName, style = MaterialTheme.typography.bodyLarge)
    }
}
