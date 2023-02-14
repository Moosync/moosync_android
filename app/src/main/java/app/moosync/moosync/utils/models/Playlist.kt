package app.moosync.moosync.utils.models

import app.moosync.moosync.utils.db.PlaylistEntity
import java.io.Serializable

data class Playlist(
    val id: String,
    val name: String
): Serializable {

    fun toDatabaseEntity(): PlaylistEntity {
        return PlaylistEntity(
            id = id,
            name = name
        )
    }

    companion object {
        fun fromDatabaseEntity(item: PlaylistEntity): Playlist {
            return Playlist(
                id = item.id,
                name = item.name
            )
        }
    }
}