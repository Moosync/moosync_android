package app.moosync.moosync.utils.services.interfaces

import app.moosync.moosync.utils.models.Song

interface MediaControls {
    fun play()
    fun pause()
    fun stop()
    fun next()
    fun previous()

    fun shuffleQueue()

    fun playSong(song: Song)
    fun addToQueue(song: Song)
}