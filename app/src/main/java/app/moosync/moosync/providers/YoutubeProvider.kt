package app.moosync.moosync.providers

import android.util.Log
import app.moosync.moosync.utils.PlayerTypes
import app.moosync.moosync.utils.models.Artist
import app.moosync.moosync.utils.models.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class YoutubeProvider : GenericProvider() {

    private val streamingService: StreamingService

    init {
        if (NewPipe.getDownloader() == null) {
            NewPipe.init(OkHttpDownloader())
        }
        streamingService = ServiceList.YouTube

    }

    override fun login(): Deferred<Unit> {
        return CoroutineScope(Dispatchers.Default).async {
            true
        }
    }

    override fun signOut(): Deferred<Unit> {
        return CoroutineScope(Dispatchers.Default).async {
            true
        }
    }

    private fun getVideoIdFromURL(url: String): String {
        return streamingService.streamLHFactory.getId(url)
    }

    private fun getChannelIdFromURL(url: String): String {
        return streamingService.channelLHFactory.getId(url)
    }

    override fun search(term: String): Deferred<ArrayList<Song>> {
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
                            listOf(Artist("youtube-author:${getChannelIdFromURL(infoItem.uploaderUrl)}", infoItem.name)),
                            null,
                            null,
                            System.currentTimeMillis(),
                            infoItem.url,
                            infoItem.thumbnailUrl,
                            PlayerTypes.YOUTUBE
                        )
                    )
                }
                Log.d("TAG", "search: ${infoItem}")
            }

            arrayList
        }
    }
}

class OkHttpDownloader() : Downloader() {
    override fun execute(request: Request): Response {
        val body = request.dataToSend()?.toRequestBody()
        val request =
            okhttp3.Request.Builder().url(request.url()).method(request.httpMethod(), body).build()

        val response = OkHttpClient().newCall(request).execute()

        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            response.body?.string(),
            response.request.url.toString()
        )
    }
}