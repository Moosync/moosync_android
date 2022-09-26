package app.moosync.moosync.ui.pages.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import app.moosync.moosync.R
import app.moosync.moosync.databinding.FragmentSongsListBinding
import app.moosync.moosync.ui.adapters.SongItemAdapter
import app.moosync.moosync.ui.base.BaseFragment
import app.moosync.moosync.utils.viewModels.SongsViewModel

class SongsList : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentSongsListBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_songs_list, container, false)
        val viewModel: SongsViewModel by activityViewModels()

        val adapter = SongItemAdapter {
            getMediaRemote()?.playSong(it)
        }
        binding.songsList.adapter = adapter

        viewModel.getSongList().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return binding.root
    }
}