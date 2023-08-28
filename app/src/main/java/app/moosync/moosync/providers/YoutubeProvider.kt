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
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
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

    private fun getPlaylistIdFromURL(url: String): String {
        return streamingService.playlistLHFactory.getId(url)
    }

    private fun parseStreamArtists(vararg artists: StreamInfoItem): ArrayList<Artist> {
        return artists.map { a ->
            Artist(
            "youtube:author:${getChannelIdFromURL(a.uploaderUrl)}",
            a.uploaderName,
            a.thumbnails.maxByOrNull { it.width }?.url) } as ArrayList<Artist>
    }

    private fun parseArtist(vararg artists: ChannelInfoItem): ArrayList<Artist> {
        return artists.map { a ->
            Artist(
                "youtube:author:${getChannelIdFromURL(a.url)}",
                a.name,
                a.thumbnails.maxByOrNull { it.width }?.url
            )} as ArrayList<Artist>
    }

    private fun parseSong(vararg songs: StreamInfoItem): ArrayList<Song> {
        return songs.map { s ->
            Song(
            "youtube:${getVideoIdFromURL(s.url)}",
            s.name,
            s.duration * 1000,
            parseStreamArtists(s),
            null,
            null,
            System.currentTimeMillis(),
            s.url,
            s.thumbnails.maxByOrNull { it.width }?.url,
            PlayerTypes.YOUTUBE
        ) } as ArrayList<Song>
    }

    override fun search(term: String): Deferred<SearchResponse> {
        return CoroutineScope(Dispatchers.Default).async {
            val songList = ArrayList<Song>()
            val artistList = ArrayList<Artist>()
            val playlistList = ArrayList<Playlist>()

            val extractor = streamingService.getSearchExtractor(term)
            extractor.fetchPage()

            for (infoItem in extractor.initialPage.items) {
                if (infoItem.infoType === InfoItem.InfoType.CHANNEL && infoItem is ChannelInfoItem) {
                    artistList.addAll(parseArtist(infoItem))
                }

                if (infoItem.infoType === InfoItem.InfoType.PLAYLIST && infoItem is PlaylistInfoItem) {
                    playlistList.add(
                        Playlist(
                            "youtube:playlist:${getPlaylistIdFromURL(infoItem.url)}",
                            infoItem.name,
                            if (infoItem.thumbnails.isNotEmpty()) infoItem.thumbnails[0].url else null)
                    )
                }

                if (infoItem.infoType === InfoItem.InfoType.STREAM && infoItem is StreamInfoItem) {
                    songList.addAll(parseSong(infoItem))
                }
            }

            SearchResponse(songList, artistList, arrayListOf(), playlistList)
        }
    }

    override fun getUserPlaylists(): Deferred<ArrayList<Playlist>> {
        return CoroutineScope(Dispatchers.Default).async {
            ArrayList()
        }
    }

    override fun getPlaylistItems(playlist: Playlist): Deferred<ArrayList<Song>> {
        return CoroutineScope(Dispatchers.Default).async {
            val songList: ArrayList<Song> = arrayListOf()
            if (!playlist.id.isNullOrBlank()) {
                val parsedPlaylistId = playlist.id.substring(17)
                val extractor = streamingService.getPlaylistExtractor("https://youtube.com/playlist?list=$parsedPlaylistId")
                extractor.fetchPage()

                songList.addAll(parseSong(*extractor.initialPage.items.filter { it.infoType === InfoItem.InfoType.STREAM && it is StreamInfoItem }.toTypedArray()))

            }

            songList
        }
    }

    override fun getArtistItems(artist: Artist): Deferred<ArrayList<Song>> {
        return CoroutineScope(Dispatchers.Default).async {
            val songList: ArrayList<Song> = arrayListOf()
            if (artist.id.isNotBlank()) {
                val parsedArtistId = artist.id.substring(15)
                val extractor = streamingService.getChannelExtractor("https://www.youtube.com/channel/$parsedArtistId")
                extractor.fetchPage()

                val tabExtractor = streamingService.getChannelTabExtractor(extractor.tabs[0])
                tabExtractor.fetchPage()

                songList.addAll(parseSong(*(tabExtractor.initialPage.items.filter { it.infoType === InfoItem.InfoType.STREAM && it is StreamInfoItem } as List<StreamInfoItem>).toTypedArray()))
            }

            songList
        }
    }

    override fun getAlbumItems(album: Album): Deferred<ArrayList<Song>> {
        return CoroutineScope(Dispatchers.Default).async {
            ArrayList()
        }
    }
}