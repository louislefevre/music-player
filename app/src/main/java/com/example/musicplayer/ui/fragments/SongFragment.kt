package com.example.musicplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.RequestManager
import com.example.musicplayer.R
import com.example.musicplayer.data.entities.Song
import com.example.musicplayer.databinding.FragmentSongBinding
import com.example.musicplayer.exoplayer.extensions.isPlaying
import com.example.musicplayer.exoplayer.extensions.toSong
import com.example.musicplayer.misc.FormatUtil.formatDuration
import com.example.musicplayer.misc.Status.SUCCESS
import com.example.musicplayer.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager
    private lateinit var binding: FragmentSongBinding
    private val mainViewModel: MainViewModel by activityViewModels()
    private var curPlayingSong: Song? = null
    private var shouldUpdateSeekbar = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        subscribeToObservers()
    }

    private fun setOnClickListeners() {
        binding.ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let { song ->
                mainViewModel.playOrToggleSong(song, true)
            }
        }

        binding.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        binding.ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurTime.text = formatDuration(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { songs ->
                            if (curPlayingSong == null && songs.isNotEmpty()) {
                                curPlayingSong = songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            it?.let {
                curPlayingSong = it.toSong()
                updateTitleAndSongImage(curPlayingSong!!)
            }
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            binding.ivPlayPauseDetail.setImageResource(
                if (it?.isPlaying == true) R.drawable.ic_pause_circle
                else R.drawable.ic_play_circle
            )
            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }

        mainViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekbar) {
                binding.seekBar.progress = it.toInt()
                binding.tvCurTime.text = formatDuration(it)
            }
        }

        mainViewModel.curSongDuration.observe(viewLifecycleOwner) {
            binding.seekBar.max = it.toInt()
            binding.tvSongDuration.text = formatDuration(it)
        }
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = getString(R.string.song_title, song.title, song.artist)
        binding.tvSongName.text = title
        glide.load(song.coverUrl).into(binding.ivSongImage)
    }
}
