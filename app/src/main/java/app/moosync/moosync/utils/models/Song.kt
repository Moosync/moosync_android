package app.moosync.moosync.utils.models

import app.moosync.moosync.utils.db.*

data class Artist(val id: Long, val name: String)
data class Album(val id: Long, val name: String)
data class Genre(val id: Long, val name: String)
data class Song(
    val _id: Long,
    val title: String,
    val duration: Long,
    val artist: List<Artist>?,
    val album: Album?,
    val genre: List<Genre>?,
    val modified: Long
) {

    fun toDatabaseEntity(): RoomSongItem {
        return RoomSongItem(
            song = SongEntity(
                _id = _id,
                title = title,
                duration = duration,
                albumId = album?.id,
                dateModified = modified
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

    override fun toString(): String {
        return super.toString()
    }

    companion object {
        fun fromDatabaseEntity(item: RoomSongItem): Song {
            return Song(
                _id = item.song._id,
                title = item.song.title,
                duration = item.song.duration,
                modified = item.song.dateModified,
                album = item.albums?.let { Album(it._id, it.name) },
                artist = item.artists?.map { Artist(it._id, it.name) },
                genre = item.genres?.map { Genre(it._id, it.name) }
            )
        }

        val emptySong = Song(-1, "", 0, null, null, null, 0)
    }
}