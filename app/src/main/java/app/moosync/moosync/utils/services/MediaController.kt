package app.moosync.moosync.utils.services

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import app.moosync.moosync.utils.PlaybackState
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.interfaces.MediaControls
import app.moosync.moosync.utils.services.interfaces.MediaPlayerCallbacks
import app.moosync.moosync.utils.services.players.PlayerListeners


class MediaController(private val mContext: Context, private val foregroundServiceCallbacks: ForegroundServiceCallbacks) {

    lateinit var queue: Queue

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

    var playerState: PlaybackState = PlaybackState.STOPPED

    private val playbackManager: PlaybackManager

    private val mediaPlayerCallbacks: MutableList<MediaPlayerCallbacks> = mutableListOf()


    private fun handleQueueChange() {
        emitInAllCallbacks { it.onQueueChange() }
    }

    private fun handleTimeChange(time: Int) {
        emitInAllCallbacks {it.onTimeChange(time)}
    }

    private fun handleSongChange(song: Song) {
        emitInAllCallbacks { it.onSongChange(queue.currentSong) }

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
                PlaybackState.PLAYING -> {
                    playbackManager.play()
                    emitInAllCallbacks {it.onPlay()}
                }
                PlaybackState.PAUSED -> {
                    playbackManager.pause()
                    emitInAllCallbacks {it.onPause()}
                }
                PlaybackState.STOPPED -> {
                    playbackManager.stop()
                    emitInAllCallbacks {it.onStop()}
                }
            }
            playerState = newState
            handlePlaybackStateChange(oldState, newState)
        }
    }

    private fun seekToPos(pos: Int) {
        playbackManager.songProgress = pos
        changePlaybackState(PlaybackState.PLAYING)

        mediaSessionHandler.updatePlayerState(true, playbackManager.songProgress)
        notificationManager.updateMetadata()
    }

    private fun toggleRepeat() {
        queue.toggleRepeat()
        val repeat = queue.repeat
        emitInAllCallbacks {
            it.onRepeatChanged(repeat)
        }
    }

    fun decideQuit(): Boolean {
        return !playbackManager.isPlaying
    }

    fun addPlayerCallbacks(callbacks: MediaPlayerCallbacks) {
        Log.d("TAG", "addPlayerCallbacks: registering callback")
        mediaPlayerCallbacks.add(callbacks)
    }

    private fun emitInAllCallbacks(c: (callback: MediaPlayerCallbacks) -> Unit) {
        for (callback in this.mediaPlayerCallbacks) {
            c.invoke(callback)
        }
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
                queue.handleSongEnded()
            }

            override fun onTimeChange(time: Int) {
                handleTimeChange(time)
            }
        })

        queue = Queue(callbacks = object : Queue.QueueCallbacks {
            override fun onCurrentSongChange(song: Song) {
                handleSongChange(song)

                val autoPlay = playerState == PlaybackState.PLAYING
                playbackManager.loadData(mContext, song, autoPlay)
            }

            override fun onQueueChange() {
                handleQueueChange()
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

            override fun seek(time: Int) {
                playbackManager.songProgress = time
            }

            override fun shuffleQueue() {
                queue.shuffle()
            }

            override fun repeat() {
                toggleRepeat()
            }

            override fun playSong(song: Song) {
                queue.playSong(song)
                changePlaybackState(PlaybackState.PLAYING)
            }

            override fun addToQueue(song: Song) {
                queue.addToQueue(song)
            }

            override fun togglePlay() {
                if (playerState === PlaybackState.PLAYING) {
                    this.pause()
                } else {
                    this.play()
                }
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

    private enum class CallbackMethods {
        ON_PLAY, ON_PAUSE, ON_STOP, ON_SONG_CHANGE, ON_QUEUE_CHANGE, ON_TIME_CHANGE, ON_REPEAT_CHANGED
    }
}