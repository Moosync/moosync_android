package app.moosync.moosync.utils.services

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import app.moosync.moosync.utils.Constants
import app.moosync.moosync.utils.models.Song

class MediaPlayerCommunicator(private val mContext: Context, private val callbacks: PlaybackStateChangeCallback): MediaSessionCompat.Callback() {
    private val playbackManager = PlaybackManager(mContext)

    override fun onPlay() {
        notifyPlaybackStateChange()
    }

    override fun onPause() {
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

    private fun loadCustomSong(bundle: Bundle?) {
        val song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getSerializable(Constants.BUNDLE_SONG_KEY, Song::class.java)
        } else {
            bundle?.getSerializable(Constants.BUNDLE_SONG_KEY) as Song
        } ?: Song.emptySong

        playbackManager.loadData(mContext, song)
        callbacks.onSongChange(song)
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            Constants.TRANSPORT_CONTROL_PLAY_SONG -> loadCustomSong(extras)
            else -> super.onCustomAction(action, extras)
        }
    }

    private fun notifyPlaybackStateChange() {
        callbacks.onPlaybackStateChange(playbackManager.isPlaying, playbackManager.songProgress)
    }

    fun release() {
        playbackManager.release()
    }

    interface PlaybackStateChangeCallback {
        fun onSongChange(song: Song)
        fun onPlaybackStateChange(isPlaying: Boolean, position: Int)
    }
}


