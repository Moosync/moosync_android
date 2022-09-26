package app.moosync.moosync.utils.services

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import app.moosync.moosync.utils.models.Song
import app.moosync.moosync.utils.services.players.PlayerListeners

class MediaPlayerCommunicator(private val mContext: Context, private val callbacks: PlaybackStateChangeCallback): MediaSessionCompat.Callback() {
    private val playerListeners = object : PlayerListeners {
        override fun onSongEnded() {
            callbacks.onSongEnded()
        }
    }

    private val playbackManager = PlaybackManager(mContext, playerListeners)

    val isPlaying: Boolean
        get() = playbackManager.isPlaying

    override fun onPlay() {
        playbackManager.play()
        notifyPlaybackStateChange()
    }

    override fun onPause() {
        playbackManager.pause()
        notifyPlaybackStateChange()
    }

    override fun onStop() {
        playbackManager.stop()
        notifyPlaybackStateChange()
    }

    override fun onSeekTo(pos: Long) {
        playbackManager.songProgress = pos.toInt()
        notifyPlaybackStateChange()
    }

    override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
        playbackManager.loadData(mContext, uri)
    }

    fun loadSong(song: Song) {
        playbackManager.loadData(mContext, song)
        callbacks.onSongChange(song)
    }

    private fun notifyPlaybackStateChange() {
        callbacks.onPlaybackStateChange(playbackManager.isPlaying, playbackManager.songProgress)
    }

    fun release() {
        Log.d("TAG", "release: Releasing communicator")
        playbackManager.release()
    }

    interface PlaybackStateChangeCallback {
        fun onSongChange(song: Song)
        fun onPlaybackStateChange(isPlaying: Boolean, position: Int)
        fun onSongEnded()
    }
}


