package com.example.musicplayer.data.entities

data class Song(
    val mediaId: String = "",
    val title: String = "",
    val artist: String = "",
    //val album: String = "",
    val songUrl: String = "",
    val coverUrl: String = ""
)
