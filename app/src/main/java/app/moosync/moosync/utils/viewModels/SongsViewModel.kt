package app.moosync.moosync.utils.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.helpers.toSongs
import app.moosync.moosync.utils.models.Song

class SongsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SongRepository(application)
    private val songList: LiveData<List<Song>> = repo.fetchAllSongs().map {
        it.toSongs()
    }

    fun getSongList(): LiveData<List<Song>> {
        return songList
    }
}