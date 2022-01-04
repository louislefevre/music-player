package com.example.musicplayer.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SwipeSongAdapter
import com.example.musicplayer.data.entities.Song
import com.example.musicplayer.exoplayer.extensions.isPlaying
import com.example.musicplayer.exoplayer.extensions.toSong
import com.example.musicplayer.misc.Constants.LAUNCHED_FROM_NOTIFICATION
import com.example.musicplayer.misc.Status.*
import com.example.musicplayer.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_player.view.*
import javax.inject.Inject

@AndroidEntryPoint  // If we inject into Android components, they must be annotated with this
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            player.isGone = when (destination.id) {
                R.id.songFragment -> true
                else -> false
            }
        }

        swipeSongAdapter = SwipeSongAdapter {
            navController.navigate(R.id.globalActionToSongFragment)
        }

        player.vpSongInfo.adapter = swipeSongAdapter
        player.vpSongInfo.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.currentList[position])
                } else {
                    curPlayingSong = swipeSongAdapter.currentList[position]
                }
            }
        })

        setOnClickListeners()
        subscribeToObservers()

        val navigateToSong = intent.getBooleanExtra(LAUNCHED_FROM_NOTIFICATION, false)
        if (navigateToSong) {
            navController.navigate(R.id.globalActionToSongFragment)
        }
    }

    private fun setOnClickListeners() {
        player.apply {
            ibTogglePlaying.setOnClickListener {
                curPlayingSong?.let {
                    mainViewModel.playOrToggleSong(it, true)
                }
            }

            ibPrevSong.setOnClickListener {
                curPlayingSong?.let {
                    mainViewModel.skipToPreviousSong()
                }
            }

            ibNextSong.setOnClickListener {
                curPlayingSong?.let {
                    mainViewModel.skipToNextSong()
                }
            }
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
                                glide.load(coverUrl).into(player.ivSongImage)
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
            glide.load(curPlayingSong?.coverUrl).into(player.ivSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }

        mainViewModel.playbackState.observe(this) {
            playbackState = it
            player.ibTogglePlaying.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause
                else R.drawable.ic_play
            )
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

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.currentList.indexOf(song)
        if (newItemIndex != -1) {  // If not in the list
            player.vpSongInfo.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun displayErrorSnackbar(message: String) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show()
    }
}
