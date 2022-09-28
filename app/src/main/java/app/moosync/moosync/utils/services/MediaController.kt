package app.moosync.moosync.utils.services

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import app.moosync.moosync.utils.PlaybackState
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.interfaces.MediaControls
import app.moosync.moosync.utils.services.players.PlayerListeners


class MediaController(private val mContext: Context, private val foregroundServiceCallbacks: ForegroundServiceCallbacks) {

    private lateinit var queue: Queue

    // Manages media session
    private var mediaSessionHandler: MediaSessionHandler = MediaSessionHandler(mContext)

    // Manages notifications
    private val notificationManager: MediaNotificationManager

    val notification: Notification?
        get() = notificationManager.notification

    // Session token required by service class
    val sessionToken: MediaSessionCompat.Token
        get() = mediaSessionHandler.sessionToken

    // Exposed controller abstraction for app to control media playback
    val controls: MediaControls

    private var playerState: PlaybackState = PlaybackState.STOPPED


    private val playbackManager: PlaybackManager

    private fun handleSongChange(song: Song) {
        mediaSessionHandler.updateMetadata(song)
        mediaSessionHandler.updatePlayerState(true)
        notificationManager.updateMetadata()
    }

    private fun handlePlaybackStateChange(oldState: PlaybackState, newState: PlaybackState) {
        val isPlaying = playerState == PlaybackState.PLAYING
        mediaSessionHandler.updatePlayerState(isPlaying, playbackManager.songProgress)

        // Start foreground service if we're just waking up from a stopped state
        if (oldState == PlaybackState.STOPPED) {
            foregroundServiceCallbacks.shouldStartForeground()
        }

        // Stop foreground service if we're going to a stopped state
        if (newState == PlaybackState.STOPPED) {
            foregroundServiceCallbacks.shouldStopForeground()
            notificationManager.clearNotification()
        } else {
            notificationManager.updateMetadata()
        }
    }

    private fun changePlaybackState(newState: PlaybackState) {
        val oldState = playerState

        if (oldState != newState) {
            when (newState) {
                PlaybackState.PLAYING -> playbackManager.play()
                PlaybackState.PAUSED -> playbackManager.pause()
                PlaybackState.STOPPED -> playbackManager.stop()
            }
            playerState = newState
            handlePlaybackStateChange(oldState, newState)
        }
    }

    private fun seekToPos(pos: Int) {
        playbackManager.songProgress = pos
        changePlaybackState(PlaybackState.PLAYING)
    }

    fun decideQuit(): Boolean {
        return !playbackManager.isPlaying
    }

    init {
        mediaSessionHandler.setCommunicatorCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                changePlaybackState(PlaybackState.PLAYING)
            }

            override fun onPause() {
                changePlaybackState(PlaybackState.PAUSED)
            }

            override fun onStop() {
                changePlaybackState(PlaybackState.STOPPED)
            }

            override fun onSkipToNext() {
                queue.next()
            }

            override fun onSkipToPrevious() {
                queue.previous()
            }

            override fun onSeekTo(pos: Long) {
                seekToPos(pos.toInt())
            }
        })

        notificationManager = MediaNotificationManager(mContext, mediaSessionHandler.sessionToken)

        playbackManager = PlaybackManager(mContext, object : PlayerListeners {
            override fun onSongEnded() {
                queue.next()
            }
        })

        queue = Queue(callbacks = object : Queue.QueueCallbacks {
            override fun onCurrentSongChange(newSong: Song) {
                handleSongChange(newSong)

                val autoPlay = playerState == PlaybackState.PLAYING
                playbackManager.loadData(mContext, newSong, autoPlay)
            }
        })


        controls = object : MediaControls {
            override fun play() {
                changePlaybackState(PlaybackState.PLAYING)
            }

            override fun pause() {
                changePlaybackState(PlaybackState.PAUSED)
            }

            override fun stop() {
                changePlaybackState(PlaybackState.STOPPED)
            }

            override fun next() {
                queue.next()
            }

            override fun previous() {
                queue.previous()
            }

            override fun playSong(song: Song) {
                queue.playNow(song)
                changePlaybackState(PlaybackState.PLAYING)
            }

            override fun addToQueue(song: Song) {
                queue.addToQueue(song)
            }
        }
    }

    fun release() {
        playbackManager.release()
        notificationManager.release()
    }

    interface ForegroundServiceCallbacks {
        fun shouldStartForeground()
        fun shouldStopForeground()
    }
}