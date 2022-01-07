package com.example.musicplayer.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreManager(private val context: Context) {

    private companion object {
        private val SONG_ID_KEY = stringPreferencesKey("song_id")
        private val SONG_POSITION_KEY = longPreferencesKey("song_position")
    }

    private val Context.dataStore by preferencesDataStore(name = "settings")

    suspend fun saveSongToPreferences(songId: String, position: Long) {
        updateDataStore(SONG_ID_KEY, songId)
        updateDataStore(SONG_POSITION_KEY, position)
    }

    fun getSongFromPreferences(): Flow<SongPreferences> {
        return getDataStorePreferences().map { preferences ->
            val songId = preferences[SONG_ID_KEY] ?: "0"
            val lastPosition = preferences[SONG_POSITION_KEY] ?: 0L
            SongPreferences(songId, lastPosition)
        }
    }

    private fun getDataStorePreferences(): Flow<Preferences> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
    }

    private suspend fun <T> updateDataStore(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }
}
