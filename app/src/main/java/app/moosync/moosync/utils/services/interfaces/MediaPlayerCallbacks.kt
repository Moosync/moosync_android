package app.moosync.moosync.utils.services.interfaces

interface MediaPlayerCallbacks {
    fun onPlay() {}
    fun onPause() {}
    fun onStop() {}
    fun onSongChange(songIndex: Int) {}
    fun onQueueChange() {}
    fun onTimeChange(time: Int) {}
}