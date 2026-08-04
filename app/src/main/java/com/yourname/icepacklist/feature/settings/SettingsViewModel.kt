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
import com.yourname.icepacklist.feature.watchlist.data.ImportState
import com.yourname.icepacklist.feature.watchlist.data.WatchlistRepository
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
    private val watchlistRepository: WatchlistRepository,
    private val hiddenItemRepository: HiddenItemRepository,
    private val moshi: Moshi
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

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState = _backupState.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting = _isImporting.asStateFlow()

    private val _importProgress = MutableStateFlow(0f)
    val importProgress = _importProgress.asStateFlow()

    fun exportBackup(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                val json = watchlistRepository.exportToJson(moshi)
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
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
            _isImporting.value = true
            _importProgress.value = 0f
            _backupState.value = BackupState.Loading
            try {
                val json = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }
                }
                if (json != null) {
                    watchlistRepository.importFromJson(json).collect { state ->
                        when (state) {
                            is ImportState.Progress -> {
                                _importProgress.value = if (state.total > 0) state.current.toFloat() / state.total else 0f
                            }
                            is ImportState.Success -> {
                                if (state.skipped > 0) {
                                    _backupState.value = BackupState.Success("Imported ${state.imported} items, ${state.skipped} skipped")
                                } else {
                                    _backupState.value = BackupState.Success("Imported ${state.imported} items")
                                }
                                _isImporting.value = false
                                apiKeyDataStore.saveLastImportTime(System.currentTimeMillis())
                            }
                            is ImportState.Error -> {
                                _backupState.value = BackupState.Error(state.message ?: "Import failed")
                                _isImporting.value = false
                            }
                        }
                    }
                } else {
                    _backupState.value = BackupState.Error("Failed to read file.")
                    _isImporting.value = false
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Failed to import backup")
                _isImporting.value = false
            }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupState.Idle
    }
}
