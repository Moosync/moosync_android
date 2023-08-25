package app.moosync.moosync.ui.pages.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import app.moosync.moosync.MainActivity
import app.moosync.moosync.R
import app.moosync.moosync.databinding.FragmentSongsListBinding
import app.moosync.moosync.ui.adapters.SongItemAdapter
import app.moosync.moosync.ui.base.BaseFragment
import app.moosync.moosync.ui.base.HeaderButtons
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.viewModels.SongsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                    getMediaRemote()?.playSong(it)

            }
        }
        binding.songsList.adapter = adapter

        setToolbar(binding.root)

        viewModel.getSongList().observe(viewLifecycleOwner) {
            val tmp = ArrayList(it)

            CoroutineScope(Dispatchers.Main).launch {
                val providers = (requireActivity() as MainActivity).providerStore.getAllProviders()
                for (p in providers) {
                    val songs = p.search("hello").await()
                    p.getUserPlaylists().await()
                    tmp.addAll(songs.songs)
                    adapter.submitList(tmp)
                }
            }
        }

        return rootView
    }
}