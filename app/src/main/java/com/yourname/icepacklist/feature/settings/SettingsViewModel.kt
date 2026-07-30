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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyDataStore: ApiKeyDataStore
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
}
