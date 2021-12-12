package com.example.musicplayer.adapters

import com.example.musicplayer.R
import kotlinx.android.synthetic.main.swipe_item.view.tvPrimary

class SwipeSongAdapter : BaseSongAdapter(R.layout.list_item) {

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.itemView.apply {
            tvPrimary.text = context.getString(R.string.song_title, song.title, song.artist)
        }
    }
}
