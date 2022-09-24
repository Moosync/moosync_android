package app.moosync.moosync.utils.services

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import app.moosync.moosync.BuildConfig
import app.moosync.moosync.R
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.Actions.ACTION_QUIT
import app.moosync.moosync.utils.services.Actions.ACTION_SHUFFLE
import app.moosync.moosync.utils.services.Actions.PLAYBACK_STATE_ACTIONS

class MediaPlayerService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: MediaNotificationManager
    private val playbackStateCompatBuilder: PlaybackStateCompat.Builder =
        PlaybackStateCompat.Builder()
    private val metadataManager = MetadataManager()

    override fun onCreate() {
        super.onCreate()

        mediaSession = createMediaSession()
        sessionToken = mediaSession.sessionToken

        mediaSession.setCallback(
            MediaPlayerCallback(
                this, playbackStateChangeCallback
            )
        )

        notificationManager = MediaNotificationManager(this, mediaSession.sessionToken)
    }

    private fun createMediaSession(): MediaSessionCompat {
        val mediaButtonReceiverComponentName = ComponentName(
            applicationContext,
            MediaButtonIntentReceiver::class.java
        )

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.component = null
        val mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, mediaButtonIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val mediaSession = MediaSessionCompat(
            this,
            BuildConfig.APPLICATION_ID,
            mediaButtonReceiverComponentName,
            mediaButtonReceiverPendingIntent
        )

        mediaSession.isActive = true
        mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent)

        return mediaSession
    }

    private val playbackStateChangeCallback = object : PlaybackStateChangeCallback {
        override fun onSongChange(song: Song) {
            metadataManager.getMetadata(this@MediaPlayerService, song, metadataFetchCallback)

            mediaSession.setPlaybackState(
                playbackStateCompatBuilder
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1F)
                    .setActions(PLAYBACK_STATE_ACTIONS)
                    .addCustomAction(PlaybackStateCompat.CustomAction.Builder(ACTION_SHUFFLE, "Shuffle", R.drawable.ic_baseline_shuffle_48).build())
                    .build()
            )
            notificationManager.updateMetadata()
        }

        override fun onPlaybackStateChange(isPlaying: Boolean, position: Int) {
            mediaSession.setPlaybackState(
                playbackStateCompatBuilder.setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    position.toLong(),
                    1F
                ).build()
            )
            notificationManager.updateMetadata()
        }
    }

    private val metadataFetchCallback = object : MetadataFetchCallback {
        override fun onCoverFetched(metadata: MediaMetadataCompat) {
            mediaSession.setMetadata(metadata)
        }

        override fun onInitialMetadata(metadata: MediaMetadataCompat) {
            mediaSession.setMetadata(metadata)
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }
}