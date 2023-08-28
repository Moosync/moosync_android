package app.moosync.moosync.utils.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
class PlaylistEntity(

    @PrimaryKey
    @ColumnInfo("playlist_id")
    val id: String,

    @ColumnInfo("name")
    val name: String,

    @ColumnInfo("coverImage")
    val coverImage: String?
)