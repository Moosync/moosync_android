package app.moosync.moosync.utils.models

import app.moosync.moosync.glide.AudioCover
import app.moosync.moosync.utils.PlayerTypes
import app.moosync.moosync.utils.db.AlbumEntity
import app.moosync.moosync.utils.db.ArtistEntity
import app.moosync.moosync.utils.db.GenreEntity
import app.moosync.moosync.utils.db.RoomSongItem
import app.moosync.moosync.utils.db.SongEntity
import com.google.gson.Gson
import java.io.Serializable


data class Artist(val id: String, val name: String): Serializable
data class Album(val id: String, val name: String): Serializable
data class Genre(val id: String, val name: String): Serializable
data class Song(
    val _id: String,
    val title: String,
    val duration: Long,
    val artist: List<Artist>?,
    val album: Album?,
    val genre: List<Genre>?,
    val modified: Long,
    val playbackUrl: String?,
    val coverImage: Any?,
    val type: PlayerTypes
): Serializable {

    private fun serializeCoverImage(): String? {
        if (coverImage != null) {
            val prefix = coverImage::class.java.name
            return "$prefix:${Gson().toJson(coverImage)}"
        }
        return null
    }

    fun toDatabaseEntity(): RoomSongItem {
        return RoomSongItem(
            song = SongEntity(
                _id = _id,
                title = title,
                duration = duration,
                albumId = album?.id,
                dateModified = modified,
                playbackUrl = playbackUrl,
                coverImage = serializeCoverImage(),
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

        private fun stripPrefix(item: String?, prefix: String?): String? {
            if (!prefix.isNullOrEmpty()) {
                return item?.replace("$prefix:", "")
            }

            return item
        }

        private fun parseCoverImage(item: String?): Any? {
            if (item != null) {
                if (item.startsWith(AudioCover::class.java.name)) {
                    return Gson().fromJson(stripPrefix(item, AudioCover::class.java.name), AudioCover::class.java)
                }

                if (item.startsWith(String::class.java.name)) {
                    return Gson().fromJson(stripPrefix(item, String::class.java.name), String::class.java)
                }

                return item
            }
            return null
        }

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
                coverImage = parseCoverImage(item.song.coverImage),
            )
        }

        val emptySong = Song("", "", 0, null, null, null, 0, null, null, PlayerTypes.LOCAL)
    }
}