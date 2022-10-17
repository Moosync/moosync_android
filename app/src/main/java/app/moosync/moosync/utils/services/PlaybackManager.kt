package app.moosync.moosync.utils.services

import android.content.Context
import app.moosync.moosync.utils.PlayerTypes
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.players.GenericPlayer
import app.moosync.moosync.utils.services.players.LocalPlayer
import app.moosync.moosync.utils.services.players.PlayerListeners
import app.moosync.moosync.utils.services.players.YoutubePlayer

class PlaybackManager(mContext: Context, private val playerListeners: PlayerListeners) {
    private val players: HashMap<PlayerTypes, GenericPlayer> = hashMapOf(Pair(PlayerTypes.LOCAL, LocalPlayer()), Pair(PlayerTypes.YOUTUBE, YoutubePlayer(mContext)))

    private var activePlayerType: PlayerTypes = PlayerTypes.LOCAL
    private val activePlayer: GenericPlayer
    get() = players[activePlayerType]!!

    var songProgress: Int
    get() { return activePlayer.progress }
    set(value) { activePlayer.progress = value }

    init {
        activePlayer.setPlayerListeners(playerListeners)
    }

    val isPlaying: Boolean
        get() = activePlayer.isPlaying

    fun stop() {
        activePlayer.stop()
    }

    fun release() {
        players.forEach {
            it.value.release()
        }
    }

    fun pause() {
        activePlayer.pause()
    }

    fun play() {
        activePlayer.play()
    }

    fun repeat() {
        songProgress = 0
    }

    private fun switchActivePlayer(newType: PlayerTypes) {
        if (activePlayerType != newType) {
            activePlayer.stop()
            activePlayer.removePlayerListeners()
            activePlayerType = newType
            activePlayer.setPlayerListeners(playerListeners)
        }
    }

    fun loadData(mContext: Context, data: Any, autoPlay: Boolean) {
        if (data is Song) {
            if (data.playbackUrl != null) {
                switchActivePlayer(data.type)
                activePlayer.load(mContext, data.playbackUrl, autoPlay)
            }
        } else {
            for (p in players) {
                if (p.value.canPlayData(data)) {
                    switchActivePlayer(p.key)
                    break
                }
            }

            activePlayer.load(mContext, data, autoPlay)
        }
    }
}