package app.moosync.moosync.utils.services.interfaces

import app.moosync.moosync.utils.models.Song

interface MediaPlayerCallbacks {
    fun onPlay() {}
    fun onPause() {}
    fun onStop() {}
    fun onSongChange(song: Song?) {}
    fun onQueueChange() {}
    fun onTimeChange(time: Int) {}
}