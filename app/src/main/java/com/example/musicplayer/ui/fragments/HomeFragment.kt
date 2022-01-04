package com.example.musicplayer.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapterFactory
import com.example.musicplayer.misc.Status
import com.example.musicplayer.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.allSongsProgressBar
import kotlinx.android.synthetic.main.fragment_home.rvAllSongs
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val mainViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var songAdapterFactory: SongAdapterFactory
    lateinit var songAdapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songAdapter = songAdapterFactory.create {
            mainViewModel.playOrToggleSong(it)
            findNavController().navigate(R.id.globalActionToSongFragment)
        }
        setupRecyclerView()
        subscribeToObservers()
    }

    private fun setupRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { songs ->
            when (songs.status) {
                Status.SUCCESS -> {
                    allSongsProgressBar.isVisible = false
                    songs.data?.let {
                        songAdapter.submitList(it)
                    }
                }
                Status.LOADING -> allSongsProgressBar.isVisible = true
                Status.ERROR -> Unit
            }
        }
    }
}
