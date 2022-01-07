package com.example.musicplayer.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.musicplayer.data.entities.Song

class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song) = oldItem.mediaId == newItem.mediaId
    override fun areContentsTheSame(oldItem: Song, newItem: Song) = oldItem.hashCode() == newItem.hashCode()
}
