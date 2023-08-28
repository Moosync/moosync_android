package app.moosync.moosync.providers

import android.content.Context
import app.moosync.moosync.utils.PlayerTypes
import app.moosync.moosync.utils.helpers.OkHttpDownloader
import app.moosync.moosync.utils.models.Album
import app.moosync.moosync.utils.models.Artist
import app.moosync.moosync.utils.models.Playlist
import app.moosync.moosync.utils.models.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class YoutubeProvider(context: Context) : GenericProvider(context) {

    private val streamingService: StreamingService

    init {
        if (NewPipe.getDownloader() == null) {
            NewPipe.init(OkHttpDownloader())
        }
        streamingService = ServiceList.YouTube

    }

    override fun login(): Deferred<Unit> {
        return CoroutineScope(Dispatchers.Default).async {

        }
    }

    override fun signOut(): Deferred<Unit> {
        return CoroutineScope(Dispatchers.Default).async {

        }
    }

    override fun matches(id: String): Boolean {
        return id.startsWith("youtube:")
    }

    private fun getVideoIdFromURL(url: String): String {
        return streamingService.streamLHFactory.getId(url)
    }

    private fun getChannelIdFromURL(url: String): String {
        return streamingService.channelLHFactory.getId(url)
    }
    override fun search(term: String): Deferred<SearchResponse> {
        return CoroutineScope(Dispatchers.Default).async {
            val arrayList = ArrayList<Song>()

            val extractor = streamingService.getSearchExtractor(term)
            extractor.fetchPage()

            for (infoItem in extractor.initialPage.items) {
                if (infoItem is StreamInfoItem) {
                    arrayList.add(
                        Song(
                            "youtube:${getVideoIdFromURL(infoItem.url)}",
                            infoItem.name,
                            infoItem.duration * 1000,
                            listOf(Artist("youtube-author:${getChannelIdFromURL(infoItem.uploaderUrl)}", infoItem.uploaderName)),
                            null,
                            null,
                            System.currentTimeMillis(),
                            infoItem.url,
                            infoItem.thumbnails[0]?.url,
                            PlayerTypes.YOUTUBE
                        )
                    )
                }
            }

            SearchResponse(arrayList, arrayListOf(), arrayListOf(), arrayListOf())
        }
    }

    override fun getUserPlaylists(): Deferred<ArrayList<Playlist>> {
        return CoroutineScope(Dispatchers.Default).async {
            ArrayList()
        }
    }

    override fun getPlaylistItems(playlist: Playlist): Deferred<ArrayList<Song>> {
        return CoroutineScope(Dispatchers.Default).async {
            ArrayList()
        }
    }

    override fun getArtistItems(artist: Artist): Deferred<ArrayList<Song>> {
        return CoroutineScope(Dispatchers.Default).async {
            ArrayList()
        }
    }

    override fun getAlbumItems(album: Album): Deferred<ArrayList<Song>> {
        return CoroutineScope(Dispatchers.Default).async {
            ArrayList()
        }
    }
}