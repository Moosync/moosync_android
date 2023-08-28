package app.moosync.moosync.providers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.Uri.Builder
import android.util.Log
import androidx.core.content.ContextCompat
import app.moosync.moosync.ui.base.BaseMainActivity
import app.moosync.moosync.utils.PlayerTypes
import app.moosync.moosync.utils.db.Secure
import app.moosync.moosync.utils.helpers.DeeplinkHandler
import app.moosync.moosync.utils.helpers.get
import app.moosync.moosync.utils.helpers.postForm
import app.moosync.moosync.utils.models.Album
import app.moosync.moosync.utils.models.Artist
import app.moosync.moosync.utils.models.Playlist
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.responses.spotify.SpotifyAccessTokenResponse
import app.moosync.moosync.utils.responses.spotify.SpotifyPlaylistItemResponse
import app.moosync.moosync.utils.responses.spotify.SpotifySearchResponse
import app.moosync.moosync.utils.responses.spotify.SpotifyUserPlaylistResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.time.Instant
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class SpotifyProvider(context: Context): GenericProvider(context) {

    private val CLIENT_ID = "95a392e7e0204fa28eee2db984ce4e00"
    private val CLIENT_SECRET = "dc91ac5fdf9547c9aa565197af01d887"
    private val TAG = "SpotifyProvider"

    private val ACCOUNTS_BASE_URL = "accounts.spotify.com"
    private val API_BASE_URL = "api.spotify.com"

    private var refreshToken: String?
    private var accessToken: String?
    private var tokenExpiry: Instant?

    val loggedIn: Boolean
        get() {
            return !accessToken.isNullOrBlank()
        }

    init {
        val secure = Secure(context)
        accessToken = secure.get("SpotifyAccessToken")
        refreshToken = secure.get("SpotifyRefreshToken")
        tokenExpiry = Instant.ofEpochMilli(secure.getLong("SpotifyTokenExpiry"))
    }

    private fun generateLoginURL(state: String): Uri {
        return Builder()
            .scheme("https")
            .authority("accounts.spotify.com")
            .appendPath("authorize")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("scope", "playlist-read-private user-top-read user-library-read user-read-private")
            .appendQueryParameter("redirect_uri", "https://moosync.app/spotify")
            .appendQueryParameter("state", state)
            .build()
    }

    private suspend fun getAccessToken(code: String, state: String?, refresh: Boolean = false): SpotifyAccessTokenResponse {
        return postForm(SpotifyAccessTokenResponse::class.java, ACCOUNTS_BASE_URL, "api/token", mapOf(
            "client_id" to CLIENT_ID,
            "redirect_uri" to "https://moosync.app/spotify",
            "grant_type" to if (!refresh) "authorization_code" else "refresh_token",
            "client_secret" to CLIENT_SECRET,

            if (!refresh) "code" to code else "refresh_token" to code,
            if (!state.isNullOrBlank()) "code_verifier" to state else "" to ""
        )).await()
    }

    private fun saveTokenResponse(tokenResponse: SpotifyAccessTokenResponse) {
        val accessTokenT = tokenResponse.access_token
        val refreshTokenT = tokenResponse.refresh_token
        val expiresIn = Instant.now().plusSeconds(tokenResponse.expires_in.toLong())

        val secureStorage = Secure(context)

        if (!accessTokenT.isNullOrBlank()) {
            secureStorage.set("SpotifyAccessToken", accessTokenT)
        }

        if (!refreshTokenT.isNullOrBlank()) {
            secureStorage.set("SpotifyRefreshToken", refreshTokenT)
        }

        secureStorage.set("SpotifyTokenExpiry", expiresIn.toEpochMilli())

        accessToken = accessTokenT
        refreshToken = refreshTokenT
        tokenExpiry = expiresIn
    }

    override fun login(): Deferred<Unit> {
        return CoroutineScope(Dispatchers.Default).async {

            if (!accessToken.isNullOrBlank() && Instant.now() <= tokenExpiry) {
                Log.d(TAG, "login: Already have tokens")
                return@async
            }

            if (!refreshToken.isNullOrBlank()) {
                val token = getAccessToken(refreshToken!!, null, true)
                Log.d(TAG, "login: $token")
                saveTokenResponse(token)
                return@async
            }

            val state = "Moosync${UUID.randomUUID()}"
            val loginURI = generateLoginURL(state)

            val code = suspendCoroutine<String> { continuation ->
                DeeplinkHandler.addListener("spotifyoauthcallback") {
                    Log.d(TAG, "login: Got login callback ${it.getQueryParameter("state")}, $state, ${state == it.getQueryParameter("state")}")
                    if (it.getQueryParameter("state")?.trim() != state) {
                        continuation.resumeWithException(Error("State mismatched. Refusing to validate user"))
                    }

                    val code = it.getQueryParameter("code")
                    if (code.isNullOrBlank()) {
                        continuation.resumeWithException(Error("Empty code returned by Spotify"))
                    } else {
                        continuation.resume(code)
                    }
                }

                val browserIntent = Intent(Intent.ACTION_VIEW, loginURI)
                ContextCompat.startActivity(context, browserIntent, null)
            }

            saveTokenResponse(getAccessToken(code, state))
        }
    }

    override fun signOut(): Deferred<Unit> {
        TODO("Not yet implemented")
    }

    override fun matches(id: String): Boolean {
        return id.startsWith("spotify:")
    }

    private fun parseArtists(vararg items: SpotifySearchResponse.SpotifyArtist): ArrayList<Artist>  {
        return items.map { a -> Artist("spotify:artist:${a.id}", a.name, if (a.images?.isNotEmpty() == true) a.images[0].url else null) } as ArrayList<Artist>
    }

    private fun parseAlbums(vararg items: SpotifySearchResponse.SpotifyAlbum): ArrayList<Album> {
        return items.map { Album("spotify:album:${it.id}", it.name) } as ArrayList<Album>
    }

    private fun parseSongs(vararg items: SpotifySearchResponse.SpotifySong): ArrayList<Song> {
        return items.map {
            Song(
                "spotify:${it.id}",
                it.name,
                it.duration_ms.toLong(),
                parseArtists(*it.artists.toTypedArray()),
                parseAlbums(it.album)[0],
                null,
                Instant.now().toEpochMilli(),
                "spotify::${it.id}",
                it.album.images?.get(0)?.url,
                PlayerTypes.YOUTUBE
                )
        } as ArrayList<Song>
    }
    
    private fun parsePlaylists(vararg items: SpotifyUserPlaylistResponse.Item): ArrayList<Playlist> {
        return items.map { Playlist("spotify:playlist:${it.id}", it.name, if (it.images.isNotEmpty()) it.images[0].url else null) } as ArrayList<Playlist>
    }

    override fun search(term: String): Deferred<SearchResponse> {
        return CoroutineScope(Dispatchers.Default).async {
            val songList: ArrayList<Song> = ArrayList()
            val artistList: ArrayList<Artist> = ArrayList()
            val albumList: ArrayList<Album> = ArrayList()
            val playlistList: ArrayList<Playlist> = ArrayList()

            if (loggedIn) {
                val resp = get(SpotifySearchResponse::class.java, API_BASE_URL, "v1/search", mapOf(
                    "q" to term,
                    "type" to "album,artist,playlist,track",
                    "limit" to 50,
                    "include_external" to "audio"
                ), mapOf(
                    "Authorization" to "Bearer $accessToken"
                )).await()

                if (resp.tracks?.items?.isNotEmpty() == true) {
                    songList.addAll(parseSongs(*resp.tracks.items.toTypedArray()))
                }

                if (resp.albums?.items?.isNotEmpty() == true) {
                    albumList.addAll(parseAlbums(*resp.albums.items.toTypedArray()))
                }

                if (resp.artists?.items?.isNotEmpty() == true) {
                    artistList.addAll(parseArtists(*resp.artists.items.toTypedArray()))
                }
                
                if (resp.playlists?.items?.isNotEmpty() == true) {
                    playlistList.addAll(parsePlaylists(*resp.playlists.items.toTypedArray()))
                }
            }

            SearchResponse(songList, artistList, albumList, playlistList)
        }
    }

    override fun getUserPlaylists(): Deferred<ArrayList<Playlist>> {
        return CoroutineScope(Dispatchers.Default).async {
            val playlistList: ArrayList<Playlist> = arrayListOf()
            if (loggedIn) {
                val resp = get(
                    SpotifyUserPlaylistResponse::class.java,
                    API_BASE_URL,
                    "v1/me/playlists",
                    mapOf("limit" to 50),
                    mapOf(
                        "Authorization" to "Bearer $accessToken"
                    )
                ).await()

                if (resp.items?.isNotEmpty() == true) {
                     playlistList.addAll(parsePlaylists(*resp.items.toTypedArray()))
                }
            }

            playlistList
        }
    }

    override fun getPlaylistItems(playlist: Playlist): Deferred<ArrayList<Song>> {
        return CoroutineScope(Dispatchers.Default).async {
            val songList: ArrayList<Song> = arrayListOf()
            if (loggedIn) {
                val parsedPlaylistId = playlist.id?.substring(17)
                if (!parsedPlaylistId.isNullOrBlank()) {
                    val resp = get(
                        SpotifyPlaylistItemResponse::class.java,
                        API_BASE_URL,
                        "v1/playlists/${parsedPlaylistId}/tracks",
                        emptyMap(),
                        mapOf(
                            "Authorization" to "Bearer $accessToken"
                        )
                    ).await()


                    if (resp.items?.isNotEmpty() == true) {
                        songList.addAll(parseSongs(*resp.items.map { it.track }.toTypedArray()))
                    }
                }
            }

            songList
        }
    }

    override fun getArtistItems(artist: Artist): Deferred<ArrayList<Song>> {
        TODO("Not yet implemented")
    }

    override fun getAlbumItems(album: Album): Deferred<ArrayList<Song>> {
        TODO("Not yet implemented")
    }

    override fun prePlaybackTransformation(song: Song): Deferred<Song?> {
        return CoroutineScope(Dispatchers.Default).async {
            if (context is BaseMainActivity) {
                val youtubeProvider = context.providerStore.getProviderForId("youtube:")
                if (youtubeProvider != null) {
                    val searchResult =
                        youtubeProvider.search("${song.artist?.joinToString(", ")} - ${song.title}")
                            .await()
                    val newSong = if (searchResult.songs.size > 0) searchResult.songs[0] else null
                    if (newSong != null) {
                        return@async Song(
                            song._id,
                            song.title,
                            newSong.duration,
                            song.artist,
                            song.album,
                            song.genre,
                            song.modified,
                            newSong.playbackUrl,
                            song.coverImage,
                            newSong.type
                        )
                    }
                }
            }
            null
        }
    }
}