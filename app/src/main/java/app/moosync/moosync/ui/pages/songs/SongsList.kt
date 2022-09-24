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
import app.moosync.moosync.utils.services.BundleConstants
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
            bundle.putLong(BundleConstants.BUNDLE_SONG_ID_KEY, it._id)
            MediaControllerCompat.getMediaController(requireActivity()).transportControls.playFromUri(
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    it._id
                ), bundle)
        }
        binding.songsList.adapter = adapter

        viewModel.getSongList().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return binding.root
    }
}