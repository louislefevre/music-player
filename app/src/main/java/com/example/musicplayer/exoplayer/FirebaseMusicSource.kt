package com.example.musicplayer.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import com.example.musicplayer.data.remote.MusicDatabase
import com.example.musicplayer.exoplayer.State.STATE_CREATED
import com.example.musicplayer.exoplayer.State.STATE_ERROR
import com.example.musicplayer.exoplayer.State.STATE_INITIALISED
import com.example.musicplayer.exoplayer.State.STATE_INITIALISING
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private enum class State {
    STATE_CREATED,
    STATE_INITIALISING,
    STATE_INITIALISED,
    STATE_ERROR
}

class FirebaseMusicSource @Inject constructor(private val musicDatabase: MusicDatabase) {

    var songs = emptyList<MediaMetadataCompat>()
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()
    private var state: State = STATE_CREATED
        set(value) {
            field = value
            if (value == STATE_INITIALISED || value == STATE_ERROR) {
                // Ensures only one thread can access listeners at one time
                synchronized(onReadyListeners) {
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALISED)
                    }
                }
            }
        }

    // Perform action when music source is ready
    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALISING) {
            onReadyListeners += action
            false
        } else {
            action(state == STATE_INITIALISED)
            true
        }
    }

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_INITIALISING
        val allSongs = musicDatabase.getAllSongs()
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_ARTIST, song.artist)
                //.putString(METADATA_KEY_ALBUM, song.album)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.coverUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.artist)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.artist)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.coverUrl)
                .build()
        }

        // Switch context so that listeners are called on main thread
        // Fix for "SimpleExoPlayer: Player is accessed on the wrong thread"
        withContext(Dispatchers.Main) {
            state = STATE_INITIALISED
        }
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI)))
            concatMediaSource.addMediaSource(mediaSource)
        }
        return concatMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.description.mediaUri)
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()
}
