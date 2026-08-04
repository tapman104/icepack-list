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
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme") // Legacy
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val ADULT_CONTENT_KEY = booleanPreferencesKey("adult_content")
    }

    val apiKey: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[API_KEY] }
        
    val lastImportTime: Flow<Long?> = context.dataStore.data
        .map { prefs -> prefs[LAST_IMPORT_TIME] }

    // Unchanged — kept for backward compatibility
    val contentFilter: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[CONTENT_FILTER_KEY] ?: "All" }

    val homeContentFilter: Flow<Set<ContentFilter>> = context.dataStore.data
        .map { prefs -> ContentFilter.fromKeys(prefs[HOME_CONTENT_FILTER_KEY] ?: "ALL") }

    val searchContentFilter: Flow<Set<ContentFilter>> = context.dataStore.data
        .map { prefs -> ContentFilter.fromKeys(prefs[SEARCH_CONTENT_FILTER_KEY] ?: "ALL") }

    val recommendationsContentFilter: Flow<Set<ContentFilter>> = context.dataStore.data
        .map { prefs -> ContentFilter.fromKeys(prefs[RECOMMENDATIONS_CONTENT_FILTER_KEY] ?: "ALL") }
        
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[DARK_THEME_KEY] ?: true }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { prefs ->
            try {
                ThemeMode.valueOf(prefs[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }

    val adultContentEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[ADULT_CONTENT_KEY] ?: false }

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

    // Unchanged — old shared key kept for backward compatibility
    suspend fun setContentFilter(value: String) {
        context.dataStore.edit { prefs ->
            prefs[CONTENT_FILTER_KEY] = value
        }
    }

    suspend fun setHomeContentFilter(filters: Set<ContentFilter>) {
        context.dataStore.edit { prefs ->
            prefs[HOME_CONTENT_FILTER_KEY] = ContentFilter.toKey(filters)
        }
    }

    suspend fun setSearchContentFilter(filters: Set<ContentFilter>) {
        context.dataStore.edit { prefs ->
            prefs[SEARCH_CONTENT_FILTER_KEY] = ContentFilter.toKey(filters)
        }
    }

    suspend fun setRecommendationsContentFilter(filters: Set<ContentFilter>) {
        context.dataStore.edit { prefs ->
            prefs[RECOMMENDATIONS_CONTENT_FILTER_KEY] = ContentFilter.toKey(filters)
        }
    }
    
    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_THEME_KEY] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun setAdultContentEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ADULT_CONTENT_KEY] = enabled
        }
    }
}
