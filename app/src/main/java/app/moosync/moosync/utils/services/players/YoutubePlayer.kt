package app.moosync.moosync.utils.services.players

import android.content.Context
import android.util.Log
import app.moosync.moosync.utils.PlaybackState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class YoutubePlayer(mContext: Context) : GenericPlayer() {
    private val _playerView = YouTubePlayerView(mContext)
    private var playerInstance: YouTubePlayer? = null
    private var isInitialized = false

    private var shouldPlayOnVideoLoad = false
    private var isLoadingVideo = false
    private val playerStateActionQueue = arrayListOf<PlaybackState>()


    init {
        _playerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                playerInstance = youTubePlayer
                isInitialized = true
            }
        })
    }

    private var _progress = 0
    override var progress: Int
        get() = _progress
        set(value) {
            playerInstance?.seekTo(value.toFloat())
        }


    private var _isPlaying: Boolean = false
    override val isPlaying: Boolean
        get() = _isPlaying


    private fun isVideoId(videoId: Any): String? {
        if (videoId is String && videoId.length == 11) return videoId
        return null
    }

    override fun canPlayData(data: Any): Boolean {
        return isVideoId(data) != null
    }

    override fun load(mContext: Context, data: Any, autoPlay: Boolean) {
        val videoId = isVideoId(data)
        if (isInitialized && videoId != null) {
            playerInstance!!.cueVideo(videoId, 0F)
            isLoadingVideo = true
            shouldPlayOnVideoLoad = autoPlay
        }
    }

    override fun play() {
        if (isInitialized) {
            if (isLoadingVideo) {
                playerStateActionQueue.add(PlaybackState.PLAYING)
            } else {
                playerInstance!!.play()
            }
        }
    }

    override fun pause() {
        if (isInitialized) {
            if (isLoadingVideo) {
                playerStateActionQueue.add(PlaybackState.PAUSED)
            } else {
                playerInstance!!.pause()
            }
        }
    }

    override fun stop() {
        if (isInitialized) {
            if (isLoadingVideo) {
                playerStateActionQueue.add(PlaybackState.PAUSED)
            } else {
                playerInstance!!.pause()
            }
        }
    }

    override fun release() {
        Log.d("TAG", "release: releasing view")
        _playerView.release()
    }

    private var youtubePlayerListener: YoutubePlayerListener? = null

    override fun setPlayerListeners(playerListeners: PlayerListeners) {
        youtubePlayerListener = YoutubePlayerListener(playerListeners)
        playerInstance?.addListener(youtubePlayerListener!!)
    }

    override fun removePlayerListeners() {
        if (youtubePlayerListener != null) {
            playerInstance?.removeListener(youtubePlayerListener!!)
        }
    }

    private fun handleVideoLoad() {
        val tmpList = playerStateActionQueue

        if (shouldPlayOnVideoLoad) {
            if (!(tmpList.contains(PlaybackState.PAUSED) || tmpList.contains(PlaybackState.STOPPED))) {
                playerInstance!!.play()
            }
        }

        for (action in tmpList) {
            when(action) {
                PlaybackState.PLAYING -> playerInstance!!.play()
                PlaybackState.PAUSED, PlaybackState.STOPPED -> playerInstance!!.pause()
            }
        }

        isLoadingVideo = false
    }

    inner class YoutubePlayerListener(private val playerListeners: PlayerListeners) :
        YouTubePlayerListener {
        override fun onApiChange(youTubePlayer: YouTubePlayer) {}

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            _progress = second.toInt()
            playerListeners.onTimeChange(second.toInt())
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {}

        override fun onPlaybackQualityChange(
            youTubePlayer: YouTubePlayer,
            playbackQuality: PlayerConstants.PlaybackQuality
        ) {}

        override fun onPlaybackRateChange(
            youTubePlayer: YouTubePlayer,
            playbackRate: PlayerConstants.PlaybackRate
        ) {}

        override fun onReady(youTubePlayer: YouTubePlayer) {}

        override fun onStateChange(
            youTubePlayer: YouTubePlayer,
            state: PlayerConstants.PlayerState
        ) {
            _isPlaying = when (state) {
                PlayerConstants.PlayerState.UNKNOWN -> false
                PlayerConstants.PlayerState.UNSTARTED -> false
                PlayerConstants.PlayerState.ENDED -> {
                    playerListeners.onSongEnded()
                    false
                }
                PlayerConstants.PlayerState.PLAYING -> true
                PlayerConstants.PlayerState.PAUSED -> false
                PlayerConstants.PlayerState.BUFFERING -> false
                PlayerConstants.PlayerState.VIDEO_CUED -> {
                    handleVideoLoad()
                    false
                }
            }

            Log.d("TAG", "onStateChange: $state")
        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {}

        override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
            Log.d("TAG", "onVideoId: $videoId")
            youTubePlayer.play()
        }

        override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
    }
}