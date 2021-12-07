package com.example.musicplayer.data.entities

data class Song(
    val mediaId: Long = 0,
    val title: String = "",
    val artist: String = "",
    //val album: String = "",
    val songUrl: String = "",
    val coverUrl: String = ""
)
