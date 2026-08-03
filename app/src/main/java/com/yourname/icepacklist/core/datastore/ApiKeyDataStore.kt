package com.yourname.icepacklist.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "icepack_prefs")

@Singleton
class ApiKeyDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val API_KEY = stringPreferencesKey("tmdb_api_key")
        private val LAST_IMPORT_TIME = longPreferencesKey("last_import_time")
        private val CONTENT_FILTER_KEY = stringPreferencesKey("content_filter")
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        private val RECOMMENDATIONS_ENABLED_KEY = booleanPreferencesKey("recommendations_enabled")
        private val SEARCH_FILTER_ENABLED_KEY = booleanPreferencesKey("search_filter_enabled")
    }

    val apiKey: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[API_KEY] }
        
    val lastImportTime: Flow<Long?> = context.dataStore.data
        .map { prefs -> prefs[LAST_IMPORT_TIME] }

    val contentFilter: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[CONTENT_FILTER_KEY] ?: "All" }
        
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[DARK_THEME_KEY] ?: true }

    val recommendationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[RECOMMENDATIONS_ENABLED_KEY] ?: true }

    val searchFilterEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[SEARCH_FILTER_ENABLED_KEY] ?: false }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[API_KEY] = key
        }
    }

    suspend fun clearApiKey() {
        context.dataStore.edit { prefs ->
            prefs.remove(API_KEY)
        }
    }
    
    suspend fun saveLastImportTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_IMPORT_TIME] = time
        }
    }

    suspend fun setContentFilter(value: String) {
        context.dataStore.edit { prefs ->
            prefs[CONTENT_FILTER_KEY] = value
        }
    }
    
    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_THEME_KEY] = enabled
        }
    }

    suspend fun setRecommendationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[RECOMMENDATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setSearchFilterEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SEARCH_FILTER_ENABLED_KEY] = enabled
        }
    }
}
