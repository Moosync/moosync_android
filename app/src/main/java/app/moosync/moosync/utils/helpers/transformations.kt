package app.moosync.moosync.utils.helpers

import android.support.v4.media.session.MediaControllerCompat
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