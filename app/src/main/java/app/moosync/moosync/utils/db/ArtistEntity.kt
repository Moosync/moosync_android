package app.moosync.moosync.utils.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
class ArtistEntity(

    @PrimaryKey
    @ColumnInfo("artist_id")
    val _id: String,

    @ColumnInfo("name")
    val name: String,

    @ColumnInfo("coverImage")
    val coverImage: String?
)