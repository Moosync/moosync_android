package app.moosync.moosync.utils.models

import app.moosync.moosync.utils.db.PlaylistEntity
import java.io.Serializable
import java.util.UUID

data class Playlist(
    val id: String?,
    val name: String,
    val coverImage: String?
): Serializable {

    fun toDatabaseEntity(): PlaylistEntity {
        return PlaylistEntity(
            id = id ?: UUID.randomUUID().toString(),
            name = name,
            coverImage = coverImage
        )
    }

    companion object {
        fun fromDatabaseEntity(item: PlaylistEntity): Playlist {
            return Playlist(
                id = item.id,
                name = item.name,
                coverImage = item.coverImage
            )
        }
    }
}