package app.moosync.moosync.utils.services.interfaces

import app.moosync.moosync.utils.PlaybackState
import app.moosync.moosync.utils.models.Song

interface MediaServiceWrapper {
    val controls: MediaControls
    val currentSong: Song?
    val currentIndex: Int
    val queue: ArrayList<Song>
    val playbackState: PlaybackState
    val repeat: Boolean

    fun decideQuit()

    fun setMainActivityStatus(isRunning: Boolean)

    fun addMediaPlayerCallbacks(callbacks: MediaPlayerCallbacks)
}