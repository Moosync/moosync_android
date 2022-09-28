package app.moosync.moosync.utils.services

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import app.moosync.moosync.utils.PlaybackStates
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.interfaces.MediaControls
import app.moosync.moosync.utils.services.players.PlayerListeners


class MediaController(private val mContext: Context) {

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


    private val playbackManager: PlaybackManager

    private fun handleSongChange(song: Song) {
        mediaSessionHandler.updateMetadata(song)
        mediaSessionHandler.updatePlayerState(true)
        notificationManager.updateMetadata()
    }

    private fun handlePlaybackStateChange(playbackState: PlaybackStates) {
        val isPlaying = playbackState === PlaybackStates.PLAYING
        mediaSessionHandler.updatePlayerState(isPlaying, playbackManager.songProgress)
        notificationManager.updateMetadata()
    }

    private fun changePlaybackState(state: PlaybackStates) {
        when(state) {
            PlaybackStates.PLAYING -> playbackManager.play()
            PlaybackStates.PAUSED -> playbackManager.pause()
            PlaybackStates.STOPPED -> playbackManager.stop()
        }
        handlePlaybackStateChange(state)
    }

    private fun seekToPos(pos: Int) {
        playbackManager.songProgress = pos
        changePlaybackState(PlaybackStates.PLAYING)
    }


    fun decideQuit(): Boolean {
        return !playbackManager.isPlaying
    }

    init {
        mediaSessionHandler.setCommunicatorCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                changePlaybackState(PlaybackStates.PLAYING)
            }

            override fun onPause() {
                changePlaybackState(PlaybackStates.PAUSED)
            }

            override fun onStop() {
                changePlaybackState(PlaybackStates.STOPPED)
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
                playbackManager.loadData(mContext, newSong)
            }
        })


        controls = object : MediaControls {
            override fun play() {
                changePlaybackState(PlaybackStates.PLAYING)
            }

            override fun pause() {
                changePlaybackState(PlaybackStates.PAUSED)
            }

            override fun stop() {
                changePlaybackState(PlaybackStates.STOPPED)
            }

            override fun next() {
                queue.next()
            }

            override fun previous() {
                queue.previous()
            }

            override fun playSong(song: Song) {
                queue.playNow(song)
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
}