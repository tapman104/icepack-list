package com.yourname.icepacklist.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import com.yourname.icepacklist.core.datastore.ThemeMode
import kotlinx.coroutines.launch
import com.yourname.icepacklist.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToApiKey: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Appearance
            SettingsSectionTitle("Appearance")
            SettingsThemeSelector(
                currentTheme = uiState.themeMode,
                onThemeSelected = viewModel::setThemeMode
            )
            HorizontalDivider()

            // 2. API Key
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
            HorizontalDivider()

            // 3. Content
            SettingsSectionTitle("Content")
            SettingsRow(
                title = "Adult Content",
                subtitle = "Include adult content in results",
                action = {
                    Switch(
                        checked = uiState.adultContentEnabled,
                        onCheckedChange = viewModel::setAdultContentEnabled
                    )
                }
            )
            SettingsRow(
                title = "Language",
                subtitle = "English",
                action = {
                    SuggestionChip(onClick = { }, label = { Text("Coming soon") })
                }
            )
            SettingsRow(
                title = "Region",
                subtitle = "US",
                action = {
                    SuggestionChip(onClick = { }, label = { Text("Coming soon") })
                }
            )
            HorizontalDivider()

            // 4. Cache & Storage
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
            HorizontalDivider()

            // 5. About
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

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
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
            .clickable(enabled = action == null) { } // Make row clickable only if no explicit action component, for better UX
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
