package app.moosync.moosync.utils.services

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import app.moosync.moosync.utils.models.Song

data class Queue(private val queueSongItems: ArrayList<Song> = arrayListOf())

class MediaQueueManager(mContext: Context) {

    // Manages media session
    private var mediaSessionHandler: MediaSessionHandler = MediaSessionHandler(mContext)

    // Manages notifications
    private val notificationManager: MediaNotificationManager

    val notification: Notification?
        get() = notificationManager.notification

    // Updates metadata

    private val mediaPlayerCommunicator: MediaPlayerCommunicator

    // Session token required by service class
    val sessionToken: MediaSessionCompat.Token
        get() = mediaSessionHandler.sessionToken

    init {
        mediaPlayerCommunicator = MediaPlayerCommunicator(mContext, PlaybackStateHandler())
        mediaSessionHandler.setCommunicatorCallback(mediaPlayerCommunicator)

        notificationManager = MediaNotificationManager(mContext, mediaSessionHandler.sessionToken)
    }

    inner class PlaybackStateHandler : MediaPlayerCommunicator.PlaybackStateChangeCallback {
        override fun onSongChange(song: Song) {
            mediaSessionHandler.updateMetadata(song)
            mediaSessionHandler.updatePlayerState(true)
            notificationManager.updateMetadata()
        }

        override fun onPlaybackStateChange(isPlaying: Boolean, position: Int) {
            mediaSessionHandler.updatePlayerState(isPlaying, position)
            notificationManager.updateMetadata()
        }

        override fun onSongEnded() {
            Log.d("TAG", "onSongEnded: Song ended")
        }
    }

    fun playSong(song: Song) {
        mediaPlayerCommunicator.loadSong(song)
    }


    fun decideQuit(): Boolean {
        return !mediaPlayerCommunicator.isPlaying
    }

    fun release() {
        Log.d("TAG", "release: Releading queue manager")
        mediaPlayerCommunicator.release()
        notificationManager.release()
    }
}