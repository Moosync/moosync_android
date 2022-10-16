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
import app.moosync.moosync.utils.PlayerTypes
import app.moosync.moosync.utils.models.Song
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
            if (it == Song.emptySong) {
                getMediaControls()?.stop()
            } else {
                getMediaControls()?.playSong(it)
            }
        }
        binding.songsList.adapter = adapter

        viewModel.getSongList().observe(viewLifecycleOwner) {
            val tmp = ArrayList(it)
            tmp.add(0, Song.emptySong)
            tmp.add(1, Song(69, "youtube", 90, null, null, null, 0, "gHzuabZUd6c", PlayerTypes.YOUTUBE))
            adapter.submitList(tmp)
        }

        return binding.root
    }
}