package com.yourname.icepacklist.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.longPreferencesKey
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
    }

    val apiKey: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[API_KEY] }
        
    val lastImportTime: Flow<Long?> = context.dataStore.data
        .map { prefs -> prefs[LAST_IMPORT_TIME] }

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
}
