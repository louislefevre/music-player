package com.example.musicplayer.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SwipeSongAdapter
import com.example.musicplayer.data.entities.Song
import com.example.musicplayer.exoplayer.extensions.isPlaying
import com.example.musicplayer.exoplayer.extensions.toSong
import com.example.musicplayer.misc.Status.ERROR
import com.example.musicplayer.misc.Status.LOADING
import com.example.musicplayer.misc.Status.SUCCESS
import com.example.musicplayer.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.ivCurSongImage
import kotlinx.android.synthetic.main.activity_main.ivPlayPause
import kotlinx.android.synthetic.main.activity_main.rootLayout
import kotlinx.android.synthetic.main.activity_main.vpSong
import javax.inject.Inject

@AndroidEntryPoint  // If we inject into Android components, they must be annotated with this
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()

        vpSong.adapter = swipeSongAdapter
        vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.currentList[position])
                } else {
                    curPlayingSong = swipeSongAdapter.currentList[position]
                }
            }
        })

        ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.currentList.indexOf(song)
        if (newItemIndex != -1) {  // If not in the list
            vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.submitList(songs)
                            if (songs.isNotEmpty()) {
                                val coverUrl = (curPlayingSong ?: songs[0]).coverUrl
                                glide.load(coverUrl).into(ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    ERROR -> Unit
                    LOADING -> Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(this) {
            if (it == null) return@observe

            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.coverUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }

        mainViewModel.playbackState.observe(this) {
            playbackState = it

            val image = if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            ivPlayPause.setImageResource(image)
        }

        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> displayErrorSnackbar(result.message ?: getString(R.string.unknown_error))
                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> displayErrorSnackbar(result.message ?: getString(R.string.unknown_error))
                    else -> Unit
                }
            }
        }
    }

    private fun displayErrorSnackbar(message: String) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show()
    }
}
