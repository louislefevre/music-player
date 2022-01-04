package com.example.musicplayer.adapters

import android.widget.TextView
import androidx.core.view.postDelayed
import com.example.musicplayer.R
import com.example.musicplayer.data.entities.Song
import com.example.musicplayer.misc.Constants.MARQUEE_INITIAL_DELAY
import kotlinx.android.synthetic.main.swipe_item.view.*

class SwipeSongAdapter(private val onSongClicked: (Song) -> Unit) : BaseSongAdapter(R.layout.swipe_item) {

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.itemView.apply {
            tvSongTitle.apply {
                text = song.title
                setupTextViewMarquee(this)
            }
            tvSongArtist.apply {
                text = song.artist
                setupTextViewMarquee(this)
            }
            setOnClickListener {
                onSongClicked(song)
            }
        }
    }

    private fun setupTextViewMarquee(textView: TextView) {
        textView.postDelayed(MARQUEE_INITIAL_DELAY) {
            textView.isSelected = true
        }
    }
}
