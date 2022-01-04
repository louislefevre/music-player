package com.example.musicplayer.misc

import java.text.SimpleDateFormat
import java.util.Locale


object FormatUtil {

    fun formatDuration(millis: Long): String {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        return dateFormat.format(millis)
    }
}
