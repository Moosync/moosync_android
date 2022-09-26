package app.moosync.moosync.utils.services

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import app.moosync.moosync.utils.PlayerTypes
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.players.GenericPlayer
import app.moosync.moosync.utils.services.players.LocalPlayer
import app.moosync.moosync.utils.services.players.YoutubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class PlaybackManager private constructor(mContext: Context) {
    private val players: HashMap<PlayerTypes, GenericPlayer> = hashMapOf(Pair(PlayerTypes.LOCAL, LocalPlayer()), Pair(PlayerTypes.YOUTUBE, YoutubePlayer(mContext)))

    private var activePlayerType: PlayerTypes = PlayerTypes.LOCAL
    private val activePlayer: GenericPlayer
    get() = players[activePlayerType]!!

    var songProgress: Int
    get() { return activePlayer.progress }
    set(value) { activePlayer.progress = value }

    val isPlaying: Boolean
        get() { return activePlayer.isPlaying }


    companion object {
        private lateinit var INSTANCE: PlaybackManager
        private var isInitialized = false

        operator fun invoke(mContext: Context): PlaybackManager {
            if (!isInitialized) {
                INSTANCE = PlaybackManager(mContext)
                isInitialized = true
            }
            return INSTANCE
        }
    }

    fun stop() {
        activePlayer.stop()
    }

    fun release() {
        activePlayer.release()
    }

    fun loadData(mContext: Context, data: Any) {
        activePlayer.stop()

        if (data is Song) {
            if (data.playbackUrl != null) {
                activePlayerType = data.type
                activePlayer.load(mContext, data.playbackUrl)
            }
        } else {
            for (p in players) {
                if (p.value.canPlayData(data)) {
                    activePlayerType = p.key
                    break
                }
            }

            activePlayer.load(mContext, data)
        }
    }
}