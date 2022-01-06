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
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.exoplayer.extensions.isPlaying
import com.example.musicplayer.exoplayer.extensions.toSong
import com.example.musicplayer.misc.Constants.LAUNCHED_FROM_NOTIFICATION
import com.example.musicplayer.misc.FormatUtil.formatDuration
import com.example.musicplayer.misc.Status.*
import com.example.musicplayer.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint  // If we inject into Android components, they must be annotated with this
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.player.root.isGone = when (destination.id) {
                R.id.songFragment -> true
                else -> false
            }
        }

        swipeSongAdapter = SwipeSongAdapter { navigateToSongFragment() }
        binding.player.vpSongInfo.apply {
            adapter = swipeSongAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    mainViewModel.playOrToggleSong(swipeSongAdapter.currentList[position])
                }
            })
        }

        setOnClickListeners()
        subscribeToObservers()

        val navigateToSong = intent.getBooleanExtra(LAUNCHED_FROM_NOTIFICATION, false)
        if (navigateToSong) {
            navigateToSongFragment()
        }
    }

    private fun setOnClickListeners() {
        binding.player.apply {
            rlSongContainer.setOnClickListener {
                navigateToSongFragment()
            }

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
                                glide.load(coverUrl).into(binding.player.ivSongImage)
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
            glide.load(curPlayingSong?.coverUrl).into(binding.player.ivSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }

        mainViewModel.playbackState.observe(this) {
            playbackState = it
            binding.player.ibTogglePlaying.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause
                else R.drawable.ic_play
            )
        }

        mainViewModel.curPlayerPosition.observe(this) {
            binding.player.apply {
                pbSongProgress.progress = it.toInt()
                tvSongPosition.text = formatDuration(it)
            }
        }

        mainViewModel.curSongDuration.observe(this) {
            binding.player.apply {
                pbSongProgress.max = it.toInt()
            }
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
            binding.player.vpSongInfo.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun navigateToSongFragment() {
        navController.navigate(R.id.globalActionToSongFragment)
    }

    private fun displayErrorSnackbar(message: String) {
        Snackbar.make(binding.rootLayout, message, Snackbar.LENGTH_LONG).show()
    }
}
