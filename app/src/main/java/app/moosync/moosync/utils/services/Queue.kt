package app.moosync.moosync.utils.services

import app.moosync.moosync.utils.models.Song

class Queue(private val queueSongItems: ArrayList<Song> = arrayListOf(), private val callbacks: QueueCallbacks) {
    private var currentSongIndex: Int = 0
        set(value) {
            field = value
            callbacks.onCurrentSongChange(currentSong)
        }

    private val currentSong: Song
        get() = queueSongItems[currentSongIndex]

    fun playNow(song: Song) {
        currentSongIndex = addToQueue(song)
    }

    fun addToQueue(song: Song): Int {
        queueSongItems.add(song)
        return queueSongItems.size - 1
    }

    fun next() {
        if (currentSongIndex + 1 < queueSongItems.size) {
            currentSongIndex += 1
        } else {
            currentSongIndex = 0
        }
    }

    fun previous() {
        if (currentSongIndex > 0) {
            currentSongIndex -= 1
        } else {
            currentSongIndex = queueSongItems.size - 1
        }
    }

    interface QueueCallbacks {
        fun onCurrentSongChange(newSong: Song)
    }
}
