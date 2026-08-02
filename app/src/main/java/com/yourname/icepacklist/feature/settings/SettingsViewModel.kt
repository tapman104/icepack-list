package com.yourname.icepacklist.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.datastore.ApiKeyDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.content.ContentResolver
import android.net.Uri
import com.squareup.moshi.Moshi
import com.yourname.icepacklist.feature.watchlist.data.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlinx.coroutines.flow.first

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyDataStore: ApiKeyDataStore,
    private val watchlistRepository: WatchlistRepository,
    private val moshi: Moshi
) : ViewModel() {

    val savedKey: StateFlow<String?> = apiKeyDataStore.apiKey
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun saveKey(key: String) {
        viewModelScope.launch {
            apiKeyDataStore.saveApiKey(key.trim())
        }
    }

    fun clearKey() {
        viewModelScope.launch {
            apiKeyDataStore.clearApiKey()
        }
    }

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState = _backupState.asStateFlow()
    
    private val _isImporting = MutableStateFlow(false)
    val isImporting = _isImporting.asStateFlow()

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
            val lastTime = apiKeyDataStore.lastImportTime.first() ?: 0L
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime < 60_000L) {
                _backupState.value = BackupState.Error("Please wait before importing again.")
                return@launch
            }
            
            _isImporting.value = true
            _backupState.value = BackupState.Loading
            try {
                val json = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }
                }
                if (json != null) {
                    val result = watchlistRepository.importFromJson(json)
                    val imported = result.first
                    val skipped = result.second
                    if (imported >= 0) {
                        if (skipped > 0) {
                            _backupState.value = BackupState.Success("Imported $imported items, $skipped skipped")
                        } else {
                            _backupState.value = BackupState.Success("Imported $imported items")
                        }
                    } else {
                        _backupState.value = BackupState.Error("Import failed — file may be corrupted")
                    }
                } else {
                    _backupState.value = BackupState.Error("Failed to read file.")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Failed to import backup")
            } finally {
                apiKeyDataStore.saveLastImportTime(System.currentTimeMillis())
                _isImporting.value = false
            }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupState.Idle
    }
}

sealed class BackupState {
    object Idle : BackupState()
    object Loading : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}
