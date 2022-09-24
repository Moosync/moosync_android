package app.moosync.moosync.utils.services

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import app.moosync.moosync.utils.db.repository.SongRepository
import app.moosync.moosync.utils.helpers.toSong
import app.moosync.moosync.utils.models.Song

class MediaPlayerCallback(private val mContext: Context, private val callbacks: PlaybackStateChangeCallback): MediaSessionCompat.Callback() {
    private val playbackManager = PlaybackManager()
    private val songRepository = SongRepository(mContext)

    override fun onPlay() {
        playbackManager.isPlaying = true
        notifyPlaybackStateChange()
    }

    override fun onPause() {
        playbackManager.isPlaying = false
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

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
    }

    override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
        playbackManager.playFromUri(mContext, uri)

        val songId = extras?.getLong(BundleConstants.BUNDLE_SONG_ID_KEY)
        if (songId != null) {
            callbacks.onSongChange(songRepository.fetchSongById(songId).toSong())
        } else {
            callbacks.onSongChange(Song.emptySong)
        }
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        Log.d("TAG", "onCustomAction: $action")
        super.onCustomAction(action, extras)
    }

    private fun notifyPlaybackStateChange() {
        callbacks.onPlaybackStateChange(playbackManager.isPlaying, playbackManager.songProgress)
    }
}

interface PlaybackStateChangeCallback {
    fun onSongChange(song: Song)
    fun onPlaybackStateChange(isPlaying: Boolean, position: Int)
}
