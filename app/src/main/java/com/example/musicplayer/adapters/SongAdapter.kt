package com.example.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.musicplayer.data.entities.Song
import com.example.musicplayer.databinding.ListItemBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class SongAdapter @AssistedInject constructor(
    private val glide: RequestManager,
    @Assisted private val onSongClicked: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.itemView.setOnClickListener {
            onSongClicked(song)
        }
        holder.bind(song)
    }

    inner class SongViewHolder(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.apply {
                tvPrimary.text = song.title
                tvSecondary.text = song.artist
                glide.load(song.coverUrl).into(ivItemImage)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song) = oldItem.mediaId == newItem.mediaId
        override fun areContentsTheSame(oldItem: Song, newItem: Song) = oldItem.hashCode() == newItem.hashCode()
    }
}

@AssistedFactory
interface SongAdapterFactory {
    fun create(onSongClicked: (Song) -> Unit): SongAdapter
}
