package app.moosync.moosync.utils.services.interfaces

interface MediaServiceWrapper {
    val controls: MediaControls

    fun decideQuit()

    fun setMainActivityStatus(isRunning: Boolean)

    fun addMediaPlayerCallbacks(callbacks: MediaPlayerCallbacks)
}