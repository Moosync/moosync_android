package app.moosync.moosync.utils.models

import app.moosync.moosync.utils.PlayerTypes
import app.moosync.moosync.utils.db.*
import java.io.Serializable


data class Artist(val id: Long, val name: String): Serializable
data class Album(val id: Long, val name: String): Serializable
data class Genre(val id: Long, val name: String): Serializable
data class Song(
    val _id: Long,
    val title: String,
    val duration: Long,
    val artist: List<Artist>?,
    val album: Album?,
    val genre: List<Genre>?,
    val modified: Long,
    val playbackUrl: String?,
    val type: PlayerTypes
): Serializable {

    fun toDatabaseEntity(): RoomSongItem {
        return RoomSongItem(
            song = SongEntity(
                _id = _id,
                title = title,
                duration = duration,
                albumId = album?.id,
                dateModified = modified,
                playbackUrl = playbackUrl,
                type = type
            ),
            albums = album?.let { AlbumEntity(it.id, it.name) },
            artists = artist?.map {
                ArtistEntity(it.id, it.name)
            },
            genres = genre?.map {
                GenreEntity(it.id, it.name)
            }
        )
    }

    companion object {
        fun fromDatabaseEntity(item: RoomSongItem): Song {
            return Song(
                _id = item.song._id,
                title = item.song.title,
                duration = item.song.duration,
                modified = item.song.dateModified,
                playbackUrl = item.song.playbackUrl,
                type = item.song.type,
                album = item.albums?.let { Album(it._id, it.name) },
                artist = item.artists?.map { Artist(it._id, it.name) },
                genre = item.genres?.map { Genre(it._id, it.name) },
            )
        }

        val emptySong = Song(-1, "", 0, null, null, null, 0, null, PlayerTypes.LOCAL)
    }
}