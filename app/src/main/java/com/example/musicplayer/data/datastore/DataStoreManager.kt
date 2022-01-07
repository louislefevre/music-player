package com.example.musicplayer.data.datastore

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class DataStoreManager(private val context: Context) {

    private companion object {
        private val RECENT_SONG_MEDIA_ID_KEY = stringPreferencesKey("recent_song_media_id")
        private val RECENT_SONG_TITLE_KEY = stringPreferencesKey("recent_song_title")
        private val RECENT_SONG_SUBTITLE_KEY = stringPreferencesKey("recent_song_subtitle")
        private val RECENT_SONG_ICON_URI_KEY = stringPreferencesKey("recent_song_icon_uri")
        private val RECENT_SONG_POSITION_KEY = longPreferencesKey("recent_song_position")
    }

    private val Context.dataStore by preferencesDataStore(name = "settings")

    private fun asAlbumArtContentUri(file: File): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            //.authority("com.example.musicplayer.exoplayer.library.provider")
            .appendPath(file.path)
            .build()
    }

    suspend fun saveRecentSongToPreferences(description: MediaDescriptionCompat, position: Long) {
        withContext(Dispatchers.IO) {
            val localIconUri = asAlbumArtContentUri(
                Glide.with(context).asFile().load(description.iconUri).submit().get()
            )

            context.dataStore.edit { settings ->
                settings[RECENT_SONG_MEDIA_ID_KEY] = description.mediaId.toString()
                settings[RECENT_SONG_TITLE_KEY] = description.title.toString()
                settings[RECENT_SONG_SUBTITLE_KEY] = description.subtitle.toString()
                settings[RECENT_SONG_ICON_URI_KEY] = localIconUri.toString()
                settings[RECENT_SONG_POSITION_KEY] = position
            }
        }
    }

    fun getRecentSongFromPreferences(): Flow<MediaBrowserCompat.MediaItem?> {
        return getDataStorePreferences().map { preferences ->
            val mediaId = preferences[RECENT_SONG_MEDIA_ID_KEY]
            if (mediaId == null) {
                null
            } else {
                val extras = Bundle().also {
                    val position = preferences[RECENT_SONG_POSITION_KEY] ?: 0L
                    it.putLong("playback_start_position_ms", position)
                }
                val desc = MediaDescriptionCompat.Builder()
                    .setMediaId(mediaId)
                    .setTitle(preferences[RECENT_SONG_TITLE_KEY] ?: "")
                    .setSubtitle(preferences[RECENT_SONG_SUBTITLE_KEY] ?: "")
                    .setIconUri(Uri.parse(preferences[RECENT_SONG_ICON_URI_KEY] ?: ""))
                    .setExtras(extras)
                    .build()

                MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
            }
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
}
