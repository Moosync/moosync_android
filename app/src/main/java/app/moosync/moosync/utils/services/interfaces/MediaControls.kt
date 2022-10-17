package app.moosync.moosync.utils.services.interfaces

import app.moosync.moosync.utils.models.Song

interface MediaControls {
    fun play()
    fun pause()
    fun togglePlay()
    fun stop()
    fun next()
    fun previous()

    fun seek(time: Int)

    fun shuffleQueue()
    fun repeat()

    fun playSong(song: Song)
    fun addToQueue(song: Song)
}