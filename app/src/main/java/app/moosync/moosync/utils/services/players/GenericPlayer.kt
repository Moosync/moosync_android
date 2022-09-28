package app.moosync.moosync.utils.services.players

import android.content.Context

abstract class GenericPlayer {
    abstract fun canPlayData(data: Any): Boolean
    abstract fun load(mContext: Context, data: Any, autoPlay: Boolean)

    abstract fun play()
    abstract fun pause()
    abstract fun stop()
    abstract fun release()

    abstract var progress: Int
    abstract val isPlaying: Boolean

    abstract fun setPlayerListeners(playerListeners: PlayerListeners)
    abstract fun removePlayerListeners()
}

interface PlayerListeners {
    fun onSongEnded()
}