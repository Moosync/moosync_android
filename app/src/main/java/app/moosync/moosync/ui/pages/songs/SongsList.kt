package app.moosync.moosync.ui.pages.songs

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.moosync.moosync.R
import app.moosync.moosync.databinding.FragmentSongsListBinding
import app.moosync.moosync.ui.adapters.SongItemAdapter
import app.moosync.moosync.utils.Constants
import app.moosync.moosync.utils.Constants.TRANSPORT_CONTROL_PLAY_SONG
import app.moosync.moosync.utils.viewModels.SongsViewModel

class SongsList : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentSongsListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_songs_list, container, false)
        val viewModel: SongsViewModel by activityViewModels()

        val adapter = SongItemAdapter {
            val bundle = Bundle()
            bundle.putSerializable(Constants.BUNDLE_SONG_KEY, it)
            MediaControllerCompat.getMediaController(requireActivity()).transportControls.sendCustomAction(TRANSPORT_CONTROL_PLAY_SONG, bundle)
        }
        binding.songsList.adapter = adapter

        viewModel.getSongList().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return binding.root
    }
}