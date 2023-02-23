package app.moosync.moosync.providers

import app.moosync.moosync.utils.models.Playlist
import app.moosync.moosync.utils.models.Song
import kotlinx.coroutines.Deferred

abstract class GenericProvider {
    abstract fun login(): Deferred<Unit>
    abstract fun signOut(): Deferred<Unit>

    abstract fun search(term: String): Deferred<ArrayList<Song>>
    abstract fun getUserPlaylists(): Deferred<ArrayList<Playlist>>
}