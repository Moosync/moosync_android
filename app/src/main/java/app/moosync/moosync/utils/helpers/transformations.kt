package app.moosync.moosync.utils.helpers

import app.moosync.moosync.utils.db.RoomSongItem
import app.moosync.moosync.utils.models.Artist
import app.moosync.moosync.utils.models.Song

fun List<RoomSongItem>.toSongs(): List<Song> {
    return map {
        Song.fromDatabaseEntity(it)
    }
}

fun RoomSongItem.toSong(): Song {
    return Song.fromDatabaseEntity(this)
}

fun List<Artist>.toArtistString(): String {
    return joinToString(", ") {
        it.name
    }
}

fun Int.toTimeString(): String {
    val secs = this / 1000
    val secsRem = secs % 60
    val mins = secs.floorDiv(60)
    val hrs = mins.floorDiv(60)

    val secStr = "${if (secsRem < 10) "0" else ""}$secsRem"

    if (hrs == 0) {
        return "$mins:$secStr"
    }
    return "$hrs:$mins:$secStr"
}