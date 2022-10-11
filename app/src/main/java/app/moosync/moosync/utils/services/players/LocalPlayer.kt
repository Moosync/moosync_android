package app.moosync.moosync.utils.services.players

import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import java.util.*


class LocalPlayer : GenericPlayer() {
    private val playerInstance = MediaPlayer()

    override var progress: Int
    get() = playerInstance.currentPosition
    set(value) { playerInstance.seekTo(value) }


    override val isPlaying: Boolean
        get() = playerInstance.isPlaying


    override fun canPlayData(data: Any): Boolean {
        return data is Uri
    }

    private fun buildUri(id: String): Uri {
        return ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id.toLong()
        )
    }

    override fun load(mContext: Context, data: Any, autoPlay: Boolean) {
        playerInstance.reset()

        if (data is String) {
            playerInstance.setDataSource(mContext, buildUri(data))
        } else if (data is Uri) {
            playerInstance.setDataSource(mContext, data)
        }

        playerInstance.setOnPreparedListener { if (autoPlay) it.start() }
        playerInstance.prepareAsync()
    }

    private var progressTimer: Timer? = null

    private fun cancelProgressTimer() {
        progressTimer?.cancel()
        progressTimer?.purge()
        progressTimer = null
    }

    override fun setPlayerListeners(playerListeners: PlayerListeners) {
        playerInstance.setOnCompletionListener {
            playerListeners.onSongEnded()
        }

        progressTimer = Timer()
        progressTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (isPlaying) {
                    playerListeners.onTimeChange(progress)
                }
            }
        }, 0, 300)
    }

    override fun removePlayerListeners() {
        playerInstance.setOnCompletionListener(null)
        cancelProgressTimer()
    }

    override fun play() {
        playerInstance.start()
    }

    override fun pause() {
        playerInstance.pause()
    }

    override fun stop() {
        playerInstance.stop()
    }

    override fun release() {
        playerInstance.release()
    }
}