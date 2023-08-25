package app.moosync.moosync.providers

import android.content.Context
import app.moosync.moosync.utils.models.Album
import app.moosync.moosync.utils.models.Artist
import app.moosync.moosync.utils.models.Playlist
import app.moosync.moosync.utils.models.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

data class SearchResponse(val songs: ArrayList<Song>, val artists: ArrayList<Artist>, val albums: ArrayList<Album>, val playlists: ArrayList<Playlist>)

abstract class GenericProvider(protected val context: Context) {
    abstract fun login(): Deferred<Unit>
    abstract fun signOut(): Deferred<Unit>

    abstract fun matches(id: String): Boolean
    open fun prePlaybackTransformation(song: Song): Deferred<Song?> {
        return CoroutineScope(Dispatchers.Default).async {
            song
        }
    }

    abstract fun search(term: String): Deferred<SearchResponse>
    abstract fun getUserPlaylists(): Deferred<ArrayList<Playlist>>
}