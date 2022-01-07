package com.example.musicplayer.exoplayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.example.musicplayer.exoplayer.FirebaseMusicSource
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class MusicPlaybackPreparer(
    private val firebaseMusicSource: FirebaseMusicSource,
    private val recentSong: MediaBrowserCompat.MediaItem?,
    private val playerPrepared: (MediaMetadataCompat?, Long) -> Unit
) : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) {
        Log.d("Test", "Preparing")
        Log.d("Test", recentSong?.mediaId.toString())

        if (recentSong != null) {
            onPrepareFromMediaId(
                recentSong.mediaId!!,
                playWhenReady,
                recentSong.description.extras
            )
        }
    }

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        firebaseMusicSource.whenReady {
            val itemToPlay = firebaseMusicSource.songs.find { mediaId == it.description.mediaId }
            val startPosition = extras?.getLong("playback_start_position_ms", C.TIME_UNSET) ?: C.TIME_UNSET
            Log.d("Test", startPosition.toString())
            playerPrepared(itemToPlay, startPosition)
        }
    }

    override fun onCommand(player: Player, command: String, extras: Bundle?, cb: ResultReceiver?) = false
    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit
    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
}
