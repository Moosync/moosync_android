package app.moosync.moosync.utils.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Binder
import android.support.v4.media.MediaMetadataCompat
import app.moosync.moosync.glide.AudioCover
import app.moosync.moosync.glide.GlideApp
import app.moosync.moosync.utils.helpers.toArtistString
import app.moosync.moosync.utils.models.Song
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.MediaStoreSignature

class MetadataManager {
    fun getMetadata(mContext: Context, song: Song, callback: MetadataFetchCallback) {

        val builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist?.toArtistString() ?: "")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)

        callback.onInitialMetadata(builder.build())

        GlideApp.with(mContext)
            .asBitmap()
            .load(AudioCover(song._id))
            .signature(MediaStoreSignature("", song.modified, 0))
            .into(object : CustomTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, resource)
                    callback.onCoverFetched(builder.build())
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
}

interface MetadataFetchCallback {
    fun onCoverFetched(metadata: MediaMetadataCompat)
    fun onInitialMetadata(metadata: MediaMetadataCompat)
}