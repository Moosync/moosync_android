package app.moosync.moosync.ui.pages.playlists

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import app.moosync.moosync.databinding.NewPlaylistDialogBinding
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.models.Playlist

class NewPlaylistDialog(context: Context) : Dialog(context) {
    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = NewPlaylistDialogBinding.inflate(LayoutInflater.from(context))

        binding.createPlaylist.setOnClickListener {
            SongRepository(context).insertPlaylist(
                Playlist(
                    null,
                    binding.playlistTitle.text.toString()
                )
            )
            dismiss()
        }

        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }
}
