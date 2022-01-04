package com.example.musicplayer.adapters

import com.example.musicplayer.R
import com.example.musicplayer.data.entities.Song
import kotlinx.android.synthetic.main.swipe_item.view.*

class SwipeSongAdapter(private val onSongClicked: (Song) -> Unit) : BaseSongAdapter(R.layout.swipe_item) {

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.itemView.apply {
            tvSongTitle.text = song.title
            tvSongArtist.text = song.artist
            setOnClickListener {
                onSongClicked(song)
            }
        }
    }
}
