package app.moosync.moosync.utils.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.helpers.toPlaylists
import app.moosync.moosync.utils.models.Playlist

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SongRepository(application)
    private val playlistList: LiveData<List<Playlist>> = repo.fetchAllPlaylists().map {
        it.toPlaylists()
    }

    fun getSongList(): LiveData<List<Playlist>> {
        return playlistList
    }
}