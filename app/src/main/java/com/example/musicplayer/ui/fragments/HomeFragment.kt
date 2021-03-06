package com.example.musicplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapterFactory
import com.example.musicplayer.databinding.FragmentHomeBinding
import com.example.musicplayer.misc.Status
import com.example.musicplayer.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    @Inject
    lateinit var songAdapterFactory: SongAdapterFactory
    private lateinit var songAdapter: SongAdapter
    private lateinit var binding: FragmentHomeBinding
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songAdapter = songAdapterFactory.create {
            mainViewModel.playOrToggleSong(it)
            findNavController().navigate(R.id.globalActionToSongFragment)
        }
        binding.rvAllSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { songs ->
            when (songs.status) {
                Status.SUCCESS -> {
                    binding.allSongsProgressBar.isVisible = false
                    songs.data?.let {
                        songAdapter.submitList(it)
                    }
                }
                Status.LOADING -> binding.allSongsProgressBar.isVisible = true
                Status.ERROR -> Unit
            }
        }
    }
}
