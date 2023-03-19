package app.moosync.moosync.providers

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import app.moosync.moosync.utils.models.Playlist
import app.moosync.moosync.utils.models.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class SpotifyProvider(context: Context): GenericProvider(context) {

    private val CLIENT_ID = "e2a60dbeffd34cc7b1bd76a84ad6c1b2"

    override fun login(): Deferred<Unit> {
        return CoroutineScope(Dispatchers.Default).async {
            true
        }
    }

    override fun signOut(): Deferred<Unit> {
        TODO("Not yet implemented")
    }

    override fun search(term: String): Deferred<ArrayList<Song>> {
        TODO("Not yet implemented")
    }

    override fun getUserPlaylists(): Deferred<ArrayList<Playlist>> {
        TODO("Not yet implemented")
    }
}