package com.example.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.data.entities.Song
import com.example.musicplayer.databinding.SwipeItemBinding
import com.example.musicplayer.misc.Constants.MARQUEE_INITIAL_DELAY

class SwipeSongAdapter(private val onSongClicked: (Song) -> Unit) :
    ListAdapter<Song, SwipeSongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeSongAdapter.SongViewHolder {
        val binding = SwipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SwipeSongAdapter.SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.itemView.setOnClickListener {
            onSongClicked(song)
        }
        holder.bind(song)
    }

    inner class SongViewHolder(private val binding: SwipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.apply {
                tvSongTitle.apply {
                    text = song.title
                    setupTextViewMarquee(this)
                }
                tvSongArtist.apply {
                    text = song.artist
                    setupTextViewMarquee(this)
                }
            }
        }

        private fun setupTextViewMarquee(textView: TextView) {
            textView.postDelayed(MARQUEE_INITIAL_DELAY) {
                textView.isSelected = true
            }
        }
    }
}
