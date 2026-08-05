package com.yourname.icepacklist.feature.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.yourname.icepacklist.core.database.entity.HiddenItemEntity
import com.yourname.icepacklist.core.database.HiddenItemRepository
import com.yourname.icepacklist.core.datastore.ApiKeyDataStore
import com.yourname.icepacklist.core.datastore.ContentFilter
import com.yourname.icepacklist.core.datastore.ThemeMode
import com.yourname.icepacklist.feature.watchlist.data.ImportPhase
import com.yourname.icepacklist.feature.watchlist.data.ImportState
import com.yourname.icepacklist.feature.watchlist.domain.WatchlistImportUseCase
import com.yourname.icepacklist.feature.watchlist.domain.WatchlistExportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val adultContentEnabled: Boolean = false,
    val language: String = "en",
    val region: String = "US",
    val apiKeyMasked: String = "••••••••",
    val homeContentFilter: Set<ContentFilter> = setOf(ContentFilter.ALL),
    val searchContentFilter: Set<ContentFilter> = setOf(ContentFilter.ALL),
    val recommendationsContentFilter: Set<ContentFilter> = setOf(ContentFilter.ALL)
)

sealed class BackupState {
    object Idle : BackupState()
    object Loading : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyDataStore: ApiKeyDataStore,
    private val hiddenItemRepository: HiddenItemRepository,
    private val importUseCase: WatchlistImportUseCase,
    private val exportUseCase: WatchlistExportUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsState> = combine(
        apiKeyDataStore.themeMode,
        apiKeyDataStore.adultContentEnabled,
        apiKeyDataStore.apiKey,
        combine(
            apiKeyDataStore.homeContentFilter,
            apiKeyDataStore.searchContentFilter,
            apiKeyDataStore.recommendationsContentFilter
        ) { home, search, recs -> Triple(home, search, recs) }
    ) { theme, adult, key, filters ->
        val maskedKey = if (!key.isNullOrBlank() && key.length >= 4) {
            "${key.take(4)}••••••••"
        } else {
            "••••••••"
        }
        SettingsState(
            themeMode = theme,
            adultContentEnabled = adult,
            language = "en",
            region = "US",
            apiKeyMasked = maskedKey,
            homeContentFilter = filters.first,
            searchContentFilter = filters.second,
            recommendationsContentFilter = filters.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsState()
    )

    val hiddenItems: StateFlow<List<HiddenItemEntity>> = hiddenItemRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            apiKeyDataStore.setThemeMode(mode)
        }
    }

    fun unhideItem(id: Int, mediaType: String) {
        viewModelScope.launch {
            hiddenItemRepository.unhide(id, mediaType)
        }
    }

    fun setAdultContentEnabled(enabled: Boolean) {
        viewModelScope.launch {
            apiKeyDataStore.setAdultContentEnabled(enabled)
        }
    }

    fun setHomeContentFilter(filters: Set<ContentFilter>) {
        viewModelScope.launch {
            apiKeyDataStore.setHomeContentFilter(filters)
        }
    }

    fun setSearchContentFilter(filters: Set<ContentFilter>) {
        viewModelScope.launch {
            apiKeyDataStore.setSearchContentFilter(filters)
        }
    }

    fun setRecommendationsContentFilter(filters: Set<ContentFilter>) {
        viewModelScope.launch {
            apiKeyDataStore.setRecommendationsContentFilter(filters)
        }
    }

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

    fun resetImportState() {
        _importState.value = ImportState.Idle
    }

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState = _backupState.asStateFlow()

    fun exportBackup(uri: Uri, contentResolver: ContentResolver, isCsv: Boolean) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                val data = if (isCsv) exportUseCase.exportToCsv() else exportUseCase.exportToJson()
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(data.toByteArray())
                    }
                }
                _backupState.value = BackupState.Success("Backup exported successfully!")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Failed to export backup")
            }
        }
    }

    fun importBackup(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _importState.value = ImportState.Progress(0, 1, "Initializing...", ImportPhase.Parsing)
            _backupState.value = BackupState.Loading
            try {
                val json = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }
                }
                if (json != null) {
                    importUseCase.invoke(json).collect { state ->
                        when (state) {
                            is ImportState.Idle -> {}
                            is ImportState.Progress -> {
                                _importState.value = state
                            }
                            is ImportState.Success -> {
                                _importState.value = state
                                val skipped = state.skippedTitles.size
                                if (skipped > 0) {
                                    val skippedStr = if (state.skippedTitles.isNotEmpty()) {
                                        val displayNames = state.skippedTitles.take(3).joinToString(", ")
                                        val remaining = state.skippedTitles.size - 3
                                        if (remaining > 0) " (Skipped: $displayNames and $remaining more)" else " (Skipped: $displayNames)"
                                    } else ""
                                    _backupState.value = BackupState.Success("Imported ${state.importedCount} items, $skipped skipped$skippedStr")
                                } else {
                                    _backupState.value = BackupState.Success("Imported ${state.importedCount} items")
                                }
                                apiKeyDataStore.saveLastImportTime(System.currentTimeMillis())
                            }
                            is ImportState.Error -> {
                                _importState.value = state
                                _backupState.value = BackupState.Error(state.message)
                            }
                        }
                    }
                } else {
                    _backupState.value = BackupState.Error("Failed to read file.")
                    _importState.value = ImportState.Error("Failed to read file.")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Failed to import backup")
                _importState.value = ImportState.Error(e.message ?: "Failed to import backup")
            }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupState.Idle
    }
}
