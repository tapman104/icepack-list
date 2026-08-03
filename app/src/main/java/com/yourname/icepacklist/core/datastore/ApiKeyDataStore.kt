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
        private val HOME_CONTENT_FILTER_KEY = stringPreferencesKey("home_content_filter")
        private val SEARCH_CONTENT_FILTER_KEY = stringPreferencesKey("search_content_filter")
        private val RECOMMENDATIONS_CONTENT_FILTER_KEY = stringPreferencesKey("recommendations_content_filter")
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }

    val apiKey: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[API_KEY] }
        
    val lastImportTime: Flow<Long?> = context.dataStore.data
        .map { prefs -> prefs[LAST_IMPORT_TIME] }

    val contentFilter: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[CONTENT_FILTER_KEY] ?: "All" }

    val homeContentFilter: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[HOME_CONTENT_FILTER_KEY] ?: "All" }

    val searchContentFilter: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[SEARCH_CONTENT_FILTER_KEY] ?: "All" }

    val recommendationsContentFilter: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[RECOMMENDATIONS_CONTENT_FILTER_KEY] ?: "All" }
        
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[DARK_THEME_KEY] ?: true }

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

    suspend fun setHomeContentFilter(value: String) {
        context.dataStore.edit { prefs ->
            prefs[HOME_CONTENT_FILTER_KEY] = value
        }
    }

    suspend fun setSearchContentFilter(value: String) {
        context.dataStore.edit { prefs ->
            prefs[SEARCH_CONTENT_FILTER_KEY] = value
        }
    }

    suspend fun setRecommendationsContentFilter(value: String) {
        context.dataStore.edit { prefs ->
            prefs[RECOMMENDATIONS_CONTENT_FILTER_KEY] = value
        }
    }
    
    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_THEME_KEY] = enabled
        }
    }
}
