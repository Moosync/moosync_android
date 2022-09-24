package app.moosync.moosync.utils.services

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat

class PlaybackManager private constructor() {
    private val mediaPlayerInstance: MediaPlayer = MediaPlayer()

    var songProgress: Int
    get() { return mediaPlayerInstance.currentPosition }
    set(value) { mediaPlayerInstance.seekTo(value) }

    var isPlaying: Boolean
        get() { return mediaPlayerInstance.isPlaying }
        set(value) { if (value) mediaPlayerInstance.start() else mediaPlayerInstance.pause() }


    companion object {
        private lateinit var INSTANCE: PlaybackManager
        private var isInitialized = false

        operator fun invoke(): PlaybackManager {
            if (!isInitialized) {
                INSTANCE = PlaybackManager()
                isInitialized = true
            }
            return INSTANCE
        }
    }

    fun stop() {
        mediaPlayerInstance.stop()
        mediaPlayerInstance.reset()
    }

    fun release() {
        mediaPlayerInstance.release()
    }

    fun playFromUri(mContext: Context, uri: Uri) {
        mediaPlayerInstance.reset()

        mediaPlayerInstance.setDataSource(mContext, uri)
        mediaPlayerInstance.setOnPreparedListener {
            mediaPlayerInstance.start()
        }
        mediaPlayerInstance.prepareAsync()
    }
}