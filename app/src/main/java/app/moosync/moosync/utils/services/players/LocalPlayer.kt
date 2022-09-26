package app.moosync.moosync.utils.services.players

import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore

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

    override fun load(mContext: Context, data: Any) {
        playerInstance.reset()

        if (data is String) {
            playerInstance.setDataSource(mContext, buildUri(data))
        } else if (data is Uri) {
            playerInstance.setDataSource(mContext, data)
        }

        playerInstance.setOnPreparedListener { it.start() }
        playerInstance.prepareAsync()
    }

    override fun setPlayerListeners(playerListeners: PlayerListeners) {
        playerInstance.setOnCompletionListener {
            playerListeners.onSongEnded()
        }
    }

    override fun removePlayerListeners() {
        playerInstance.setOnCompletionListener(null)
    }

    override fun play() {
        playerInstance.start()
    }

    override fun pause() {
        playerInstance.pause()
    }

    override fun stop() {
        playerInstance.stop()
        playerInstance.reset()
    }

    override fun release() {
        playerInstance.release()
    }
}