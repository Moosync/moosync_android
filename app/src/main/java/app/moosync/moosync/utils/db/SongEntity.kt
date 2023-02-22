package app.moosync.moosync.utils.db

import androidx.room.*
import app.moosync.moosync.utils.PlayerTypes

@Entity(tableName = "all_songs")
class SongEntity(
    @PrimaryKey
    @ColumnInfo("song_id")
    val _id: String,

    @ColumnInfo("title")
    val title: String,

    @ColumnInfo("album_id")
    val albumId: String?,

    @ColumnInfo("duration")
    val duration: Long,

    @ColumnInfo("date_modified")
    val dateModified: Long,

    @ColumnInfo("playback_url")
    val playbackUrl: String?,

    @ColumnInfo("coverImage")
    val coverImage: String?,

    @ColumnInfo("type")
    val type: PlayerTypes
)

@Entity(primaryKeys = ["song_id", "artist_id"])
data class SongAndArtistEntity (
    val song_id: String,
    val artist_id: String
)

@Entity(primaryKeys = ["song_id", "genre_id"])
data class SongAndGenreEntity (
    val song_id: String,
    val genre_id: String
)

data class RoomSongItem(
    @Embedded
    var song: SongEntity,

    @Relation(
        parentColumn = "song_id",
        entity = ArtistEntity::class,
        entityColumn = "artist_id",
        associateBy = Junction(
            value = SongAndArtistEntity::class,
            parentColumn = "song_id",
            entityColumn = "artist_id"
        )
    )
    var artists: List<ArtistEntity>?,


    @Relation(
        parentColumn = "album_id",
        entity = AlbumEntity::class,
        entityColumn = "album_id",
    )
    var albums: AlbumEntity?,

    @Relation(
        parentColumn = "song_id",
        entity = GenreEntity::class,
        entityColumn = "genre_id",
        associateBy = Junction(
            value = SongAndGenreEntity::class,
            parentColumn = "song_id",
            entityColumn = "genre_id"
        )
    )
    var genres: List<GenreEntity>?
)




