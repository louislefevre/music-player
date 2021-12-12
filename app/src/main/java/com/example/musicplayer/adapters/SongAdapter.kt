package com.example.musicplayer.adapters

import com.bumptech.glide.RequestManager
import com.example.musicplayer.R
import com.example.musicplayer.data.entities.Song
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.android.synthetic.main.list_item.view.ivItemImage
import kotlinx.android.synthetic.main.list_item.view.tvPrimary
import kotlinx.android.synthetic.main.list_item.view.tvSecondary

class SongAdapter @AssistedInject constructor(
    private val glide: RequestManager,
    @Assisted private val onSongClicked: (Song) -> Unit
) : BaseSongAdapter(R.layout.list_item) {

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.itemView.apply {
            tvPrimary.text = song.title
            tvSecondary.text = song.artist
            glide.load(song.coverUrl).into(ivItemImage)

            setOnClickListener {
                onSongClicked(song)
            }
        }
    }
}

@AssistedFactory
interface SongAdapterFactory {
    fun create(onSongClicked: (Song) -> Unit): SongAdapter
}
