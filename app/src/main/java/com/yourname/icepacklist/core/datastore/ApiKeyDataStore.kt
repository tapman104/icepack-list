package com.yourname.icepacklist.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "icepack_prefs")

@Singleton
class ApiKeyDataStore @Inject constructor(
    private val context: Context
) {
    companion object {
        private val API_KEY = stringPreferencesKey("tmdb_api_key")
    }

    val apiKey: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[API_KEY] }

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
}
