package app.moosync.moosync.providers

import android.content.Context
import app.moosync.moosync.utils.models.Album
import app.moosync.moosync.utils.models.Artist
import app.moosync.moosync.utils.models.Playlist
import app.moosync.moosync.utils.models.Song
import kotlinx.coroutines.Deferred

data class SearchResponse(val songs: ArrayList<Song>, val artists: ArrayList<Artist>, val albums: ArrayList<Album>, val playlists: ArrayList<Playlist>)

abstract class GenericProvider(protected val context: Context) {
    abstract fun login(): Deferred<Unit>
    abstract fun signOut(): Deferred<Unit>

    abstract fun search(term: String): Deferred<SearchResponse>
    abstract fun getUserPlaylists(): Deferred<ArrayList<Playlist>>
}