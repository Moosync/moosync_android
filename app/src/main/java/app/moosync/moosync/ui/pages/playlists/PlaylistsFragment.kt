package app.moosync.moosync.ui.pages.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import app.moosync.moosync.R
import app.moosync.moosync.databinding.FragmentSongsListBinding
import app.moosync.moosync.ui.base.BaseFragment

class PlaylistsFragment: BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentSongsListBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_songs_list, container, false)

        setToolbar(binding.root)

        return binding.root
    }
}