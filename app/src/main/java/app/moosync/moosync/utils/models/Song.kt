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


data class Artist(val id: String, val name: String, val coverImage: Any?): Serializable
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

    override fun equals(other: Any?): Boolean {
        if (other is Song) return other._id === this._id
        return super.equals(other)
    }

    private fun serializeCoverImage(coverImg: Any?): String? {
        if (coverImg != null) {
            val prefix = coverImg::class.java.name
            return "$prefix:${Gson().toJson(coverImg)}"
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
                coverImage = serializeCoverImage(coverImage),
                type = type
            ),
            albums = album?.let { AlbumEntity(it.id, it.name) },
            artists = artist?.map {
                ArtistEntity(it.id, it.name, serializeCoverImage(it.coverImage))
            },
            genres = genre?.map {
                GenreEntity(it.id, it.name)
            }
        )
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (genre?.hashCode() ?: 0)
        result = 31 * result + modified.hashCode()
        result = 31 * result + (playbackUrl?.hashCode() ?: 0)
        result = 31 * result + (coverImage?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
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
                artist = item.artists?.map { Artist(it._id, it.name, it.coverImage) },
                genre = item.genres?.map { Genre(it._id, it.name) },
                coverImage = parseCoverImage(item.song.coverImage),
            )
        }

        val emptySong = Song("", "", 0, null, null, null, 0, null, null, PlayerTypes.LOCAL)
    }
}