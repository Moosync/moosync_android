package app.moosync.moosync.utils.services.interfaces

import app.moosync.moosync.utils.PlaybackState
import app.moosync.moosync.utils.models.Song

interface MediaServiceWrapper {
    val controls: MediaControls
    val currentSong: Song?
    val playbackState: PlaybackState

    fun decideQuit()

    fun setMainActivityStatus(isRunning: Boolean)

    fun addMediaPlayerCallbacks(callbacks: MediaPlayerCallbacks)
}