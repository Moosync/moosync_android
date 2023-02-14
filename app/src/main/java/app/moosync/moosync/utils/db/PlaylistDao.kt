package app.moosync.moosync.utils.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(album: PlaylistEntity)

    @Transaction
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): LiveData<List<PlaylistEntity>>
}
