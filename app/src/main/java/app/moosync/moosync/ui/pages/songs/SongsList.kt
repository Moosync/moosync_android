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
import app.moosync.moosync.ui.base.HeaderButtons
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
        rootView = binding.root

        setHeaderButtons(HeaderButtons("Add to queue", R.drawable.add_queue), HeaderButtons("Sort by", R.drawable.list_sort))

        val viewModel: SongsViewModel by activityViewModels()

        val adapter = SongItemAdapter {
            if (it == Song.emptySong) {
                getMediaControls()?.stop()
            } else {
                getMediaControls()?.playSong(it)
            }
        }
        binding.songsList.adapter = adapter

        setToolbar(binding.root)

        viewModel.getSongList().observe(viewLifecycleOwner) {
            val tmp = ArrayList(it)
            tmp.add(0, Song.emptySong)
            tmp.add(1, Song("youtube:gHzuabZUd6c", "youtube", 90, null, null, null, 0, "gHzuabZUd6c", null, PlayerTypes.YOUTUBE))
            adapter.submitList(tmp)
        }

        return rootView
    }
}