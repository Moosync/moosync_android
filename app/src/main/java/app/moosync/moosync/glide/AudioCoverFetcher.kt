package app.moosync.moosync.glide

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap.CompressFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.images.Artwork
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream


internal class AudioCoverLoader(private val mContext: Context) :
    ModelLoader<AudioCover, InputStream> {
    fun getResourceFetcher(model: AudioCover, width: Int, height: Int): DataFetcher<InputStream> {
        return AudioCoverFetcher(model, mContext, width, height)
    }

    internal class Factory(private val mContext: Context) :
        ModelLoaderFactory<AudioCover, InputStream> {
        override fun teardown() {}
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AudioCover, InputStream> {
            return AudioCoverLoader(mContext)
        }
    }

    override fun buildLoadData(
        model: AudioCover,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<InputStream> {
        return LoadData(
            ObjectKey(model.id),
            AudioCoverFetcher(model, mContext, width, height)
        )
    }

    override fun handles(model: AudioCover): Boolean {
        return model.id.isNotEmpty()
    }
}

internal class AudioCoverFetcher(
    private val model: AudioCover,
    private val mContext: Context,
    private val height: Int,
    private val width: Int
) : DataFetcher<InputStream> {
    private var stream: FileInputStream? = null
    private val id: String
        get() = model.id

    private fun fallback(uri: Uri): InputStream? {
        try {
            val mp3File = MP3File(uri.path)
            if (mp3File.hasID3v2Tag()) {
                val art: Artwork = mp3File.tag.firstArtwork
                val imageData: ByteArray = art.binaryData
                return ByteArrayInputStream(imageData)
            }
            // If there are any exceptions, we ignore them and continue to the other fallback method
        } catch (ignored: Exception) {
            // Nothing to do
        }

        // Method 2: look for album art in external files

        val parent: File = uri.path?.let { File(it).parentFile } as File
        for (fallback in FALLBACKS) {
            val cover = File(parent, fallback)
            if (cover.exists()) {
                return FileInputStream(cover)
            }
        }
        return null
    }

    override fun cleanup() {
        // already cleaned up in loadData and ByteArrayInputStream will be GC'd
        if (stream != null) {
            try {
                stream!!.close()
            } catch (ignore: IOException) {
                // can't do much about it
            }
        }
    }

    override fun cancel() {
        // cannot cancel
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    companion object {
        val FALLBACKS = arrayOf("cover.jpg", "album.jpg", "folder.jpg")
    }

    private fun getCoverImg(priority: Priority): InputStream? {

        val uri =
            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong())

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val thumb = mContext.contentResolver.loadThumbnail(uri, Size(width, height), null)
                val stream = ByteArrayOutputStream()
                thumb.compress(CompressFormat.JPEG, 100, stream)
                return ByteArrayInputStream(stream.toByteArray())
            }
        } catch (e: Exception) {
            // Nothing to do
        }

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(mContext, uri)
            val picture = retriever.embeddedPicture
            return picture?.let { ByteArrayInputStream(it) }
                ?: fallback(uri)
        } finally {
            retriever.release()
        }

    }


    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream?>) {
        val cover = getCoverImg(priority)
        if (cover == null) {
            callback.onLoadFailed(Exception("Failed to load image"))
        } else {
            callback.onDataReady(cover)
        }
    }
}