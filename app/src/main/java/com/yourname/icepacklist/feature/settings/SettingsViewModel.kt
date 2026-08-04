package com.yourname.icepacklist.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.datastore.ApiKeyDataStore
import com.yourname.icepacklist.core.datastore.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val adultContentEnabled: Boolean = false,
    val language: String = "en",
    val region: String = "US",
    val apiKeyMasked: String = "••••••••"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyDataStore: ApiKeyDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsState> = combine(
        apiKeyDataStore.themeMode,
        apiKeyDataStore.adultContentEnabled,
        apiKeyDataStore.apiKey
    ) { theme, adult, key ->
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
            apiKeyMasked = maskedKey
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsState()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            apiKeyDataStore.setThemeMode(mode)
        }
    }

    fun setAdultContentEnabled(enabled: Boolean) {
        viewModelScope.launch {
            apiKeyDataStore.setAdultContentEnabled(enabled)
        }
    }
}
