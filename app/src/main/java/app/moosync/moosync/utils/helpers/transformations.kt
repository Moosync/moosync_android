package app.moosync.moosync.utils.helpers

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import app.moosync.moosync.utils.db.PlaylistEntity
import app.moosync.moosync.utils.db.RoomSongItem
import app.moosync.moosync.utils.models.Artist
import app.moosync.moosync.utils.models.Playlist
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

fun List<PlaylistEntity>.toPlaylists(): List<Playlist> {
    return map {
        Playlist.fromDatabaseEntity(it)
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


fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = dpToPx(this) }
        top?.run { topMargin = dpToPx(this) }
        right?.run { rightMargin = dpToPx(this) }
        bottom?.run { bottomMargin = dpToPx(this) }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}
fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()