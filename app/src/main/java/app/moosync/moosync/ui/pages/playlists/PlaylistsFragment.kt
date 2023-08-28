package app.moosync.moosync.ui.pages.playlists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import app.moosync.moosync.MainActivity
import app.moosync.moosync.R
import app.moosync.moosync.databinding.FragmentPlaylistsBinding
import app.moosync.moosync.ui.adapters.PlaylistItemAdapter
import app.moosync.moosync.ui.base.BaseFragment
import app.moosync.moosync.ui.base.HeaderButtons
import app.moosync.moosync.utils.viewModels.PlaylistsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistsFragment: BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPlaylistsBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_playlists, container, false)
        rootView = binding.root

        setHeaderButtons(HeaderButtons("New Playlist", R.drawable.material_symbols_add_rounded, {
            NewPlaylistDialog(requireContext()).show()
        }))

        setToolbar(binding.root)

        val viewModel: PlaylistsViewModel by activityViewModels()

        val adapter = PlaylistItemAdapter {
            Log.d(TAG, "onCreateView: clicked ${it.name}")
        }

        binding.playlistList.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistList.adapter = adapter

        viewModel.getPlaylistList().observe(viewLifecycleOwner) {
            val tmp = ArrayList(it)
            adapter.submitList(tmp)

            CoroutineScope(Dispatchers.Main).launch {
                val providers = (requireActivity() as MainActivity).providerStore.getAllProviders()
                for (p in providers) {
                    val oldPos = tmp.size
                    val resp = p.getUserPlaylists().await()
                    tmp.addAll(resp)
                    adapter.notifyItemRangeInserted(oldPos, tmp.size - oldPos - 1)
                }
            }
        }

        return rootView
    }
}