package app.moosync.moosync.utils.services

import android.util.Log
import app.moosync.moosync.utils.models.Song

class Queue(private val queueSongItems: ArrayList<Song> = arrayListOf(), private val callbacks: QueueCallbacks) {
     var currentSongIndex: Int = -1
        set(value) {
            field = value
            callbacks.onCurrentSongChange(queueSongItems[value])
        }

    val currentSong: Song?
        get() = if (currentSongIndex >= 0) queueSongItems[currentSongIndex] else null

    private var _repeat = false
    val repeat: Boolean
    get() = _repeat

    fun playSong(song: Song) {
        currentSongIndex = addToQueue(song)
        Log.d("TAG", "playSong: $currentSong")
    }

    fun addToQueue(song: Song): Int {
        queueSongItems.add(song)
        callbacks.onQueueChange()
        return queueSongItems.size - 1
    }

    fun handleSongEnded() {
        if (repeat) {
            val song = currentSong
            if (song != null) {
                callbacks.onCurrentSongChange(song)
            }
        } else {
            this.next()
        }
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

    fun shuffle() {
        queueSongItems.shuffle()
    }

    fun toggleRepeat() {
        _repeat = !_repeat
    }

    interface QueueCallbacks {
        fun onCurrentSongChange(song: Song)
        fun onQueueChange()
    }
}
