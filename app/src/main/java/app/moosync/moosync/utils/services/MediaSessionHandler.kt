package app.moosync.moosync.utils.services

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import app.moosync.moosync.BuildConfig
import app.moosync.moosync.R
import app.moosync.moosync.utils.helpers.toArtistString
import app.moosync.moosync.utils.models.Song
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.MediaStoreSignature

class MediaSessionHandler(private val mContext: Context) {

    private val mMediaSession: MediaSessionCompat

    val sessionToken: MediaSessionCompat.Token
        get() = mMediaSession.sessionToken

    init {
        mMediaSession = createMediaSession(mContext)
    }

    fun setCommunicatorCallback(callback: MediaSessionCompat.Callback) {
        mMediaSession.setCallback(callback)
    }

    private fun createMediaSession(mContext: Context): MediaSessionCompat {
        val mediaButtonReceiverComponentName = ComponentName(
            mContext.applicationContext,
            MediaButtonIntentReceiver::class.java
        )

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.component = mediaButtonReceiverComponentName
        val mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(
            mContext.applicationContext, 0, mediaButtonIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val mediaSession = MediaSessionCompat(
            mContext,
            BuildConfig.APPLICATION_ID,
            mediaButtonReceiverComponentName,
            mediaButtonReceiverPendingIntent
        )

        mediaSession.isActive = true
        mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent)

        return mediaSession
    }

    fun updatePlayerState(isPlaying: Boolean, position: Int = 0) {
        mMediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    position.toLong(),
                    1F
                )
                .setActions(Actions.PLAYBACK_STATE_ACTIONS)
                .addCustomAction(
                    PlaybackStateCompat.CustomAction.Builder(
                        Actions.ACTION_SHUFFLE,
                        "Shuffle",
                        R.drawable.ic_baseline_shuffle_48
                    ).build()
                )
                .build()
        )
    }

    fun updateMetadata(song: Song) {
        if (song != Song.emptySong) {
            Log.d("TAG", "updateMetadata: ${song.duration}")
            val builder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(
                    MediaMetadataCompat.METADATA_KEY_ARTIST,
                    song.artist?.toArtistString() ?: ""
                )
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)

            mMediaSession.setMetadata(builder.build())

            Glide.with(mContext)
                .asBitmap()
                .load(song.coverImage)
                .signature(MediaStoreSignature("", song.modified, 0))
                .into(object : CustomTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, resource)
                        mMediaSession.setMetadata(builder.build())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        } else {
            mMediaSession.setMetadata(null)
        }
    }
}