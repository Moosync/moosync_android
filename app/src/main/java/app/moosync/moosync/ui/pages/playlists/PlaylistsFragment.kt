package app.moosync.moosync.ui.pages.playlists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import app.moosync.moosync.R
import app.moosync.moosync.databinding.FragmentSongsListBinding
import app.moosync.moosync.ui.adapters.PlaylistItemAdapter
import app.moosync.moosync.ui.base.BaseFragment
import app.moosync.moosync.ui.base.HeaderButtons
import app.moosync.moosync.utils.viewModels.PlaylistsViewModel

class PlaylistsFragment: BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentSongsListBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_songs_list, container, false)
        rootView = binding.root

        setHeaderButtons(HeaderButtons("New Playlist", R.drawable.material_symbols_add_rounded, {
            NewPlaylistDialog(requireContext()).show()
        }))

        setToolbar(binding.root)

        val viewModel: PlaylistsViewModel by activityViewModels()

        val adapter = PlaylistItemAdapter {
            Log.d(TAG, "onCreateView: clicked ${it.name}")
        }

        binding.songsList.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.songsList.adapter = adapter

        viewModel.getSongList().observe(viewLifecycleOwner) {
            val tmp = ArrayList(it)
            adapter.submitList(tmp)
        }

        return rootView
    }
}