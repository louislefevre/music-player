package com.example.musicplayer.data.remote

import android.util.Log
import com.example.musicplayer.data.entities.Song
import com.example.musicplayer.misc.Constants.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val database = FirebaseFirestore.getInstance()
    private val songCollection = database.collection(SONG_COLLECTION)

    suspend fun getAllSongs(): List<Song> {
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (ex: Exception) {
            Log.e("Database", "Failed to retrieve songs from database", ex)  // TODO: Replace with Timber
            emptyList()
        }
    }
}

// TODO: Use pagination in the future
