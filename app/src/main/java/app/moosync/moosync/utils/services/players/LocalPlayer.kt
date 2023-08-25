package app.moosync.moosync.utils.services.players

import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import java.util.Timer


class LocalPlayer : GenericPlayer() {
    private val playerInstance = MediaPlayer()

    override var progress: Int
    get() = playerInstance.currentPosition
    set(value) { playerInstance.seekTo(value) }

    private var ignoreSongEnded = false

    override val isPlaying: Boolean
        get() {
            try {
                return playerInstance.isPlaying
            } catch (e: Exception) {
                Log.e("TAG", "Error getting isPlaying:", e)
            }
            return false
        }

    private var isPlayerPrepared = false
    private val afterPreparedMethodCalls: MutableList<() -> Unit> = mutableListOf()


    override fun canPlayData(data: Any): Boolean {
        return data is Uri
    }

    private fun buildUri(id: String): Uri {
        return ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id.toLong()
        )
    }

    private fun runAfterPlayerPrepared(method: () -> Unit) {
        if (isPlayerPrepared) {
            method.invoke()
            return
        }
        afterPreparedMethodCalls.add(method)
    }

    private fun runQueuedMethods() {
        for (method in afterPreparedMethodCalls) {
            method.invoke()
        }
    }

    override fun load(mContext: Context, data: Any, autoPlay: Boolean) {
        ignoreSongEnded = true
        isPlayerPrepared = false

        playerInstance.reset()

        if (data is String) {
            playerInstance.setDataSource(mContext, buildUri(data))
        } else if (data is Uri) {
            playerInstance.setDataSource(mContext, data)
        }

        playerInstance.setOnPreparedListener {
            if (autoPlay) it.start()

            isPlayerPrepared = true
            ignoreSongEnded = false
            runQueuedMethods()
        }
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
            if (!ignoreSongEnded)  {
                playerListeners.onSongEnded()
                ignoreSongEnded = false
            }
        }

        val handler = Handler(Looper.getMainLooper())
        val runnable = object: Runnable {
            override fun run() {
                if (isPlaying) {
                    playerListeners.onTimeChange(progress)
                }
                handler.postDelayed(this, 300)
            }
        }

        runnable.run()
    }

    override fun removePlayerListeners() {
        playerInstance.setOnCompletionListener(null)
        cancelProgressTimer()
    }

    override fun play() {
        runAfterPlayerPrepared {
            playerInstance.start()
        }
    }

    override fun pause() {
        runAfterPlayerPrepared {
            playerInstance.pause()
        }
    }

    override fun stop() {
        playerInstance.stop()
    }

    override fun release() {
        playerInstance.release()
    }
}