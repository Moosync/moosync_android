package app.moosync.moosync.utils.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
class AlbumEntity(

    @PrimaryKey
    @ColumnInfo("album_id")
    val _id: Long,

    @ColumnInfo("name")
    val name: String
)